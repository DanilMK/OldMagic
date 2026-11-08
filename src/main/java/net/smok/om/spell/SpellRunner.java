package net.smok.om.spell;


import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.smok.om.Debug;
import net.smok.om.entity.SpellProjectile;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.HitHolder;
import net.smok.om.utils.StoredValueType;
import net.smok.om.utils.StoredValues;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SpellRunner {

    // Статический кодек для полной сериализации ранера
    public static final Codec<SpellRunner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(r -> r.action),
            UUIDUtil.CODEC.fieldOf("actor_uuid").forGetter(r -> r.actorUUID),
            ItemStack.CODEC.fieldOf("wand").forGetter(r -> r.wand),
            Codec.INT.fieldOf("random_seed").forGetter(r -> r.randomSeed),
            Codec.INT.fieldOf("last_index").forGetter(r -> r.lastIndex),
            Codec.INT.fieldOf("wait_ticks_left").forGetter(r -> r.waitTicksLeft),
            StoredValues.CODEC.fieldOf("stored_values").forGetter(r -> r.storedValues)
    ).apply(instance, SpellRunner::new));

    // Константные данные каста
    private final Holder<SpellAction> action;
    private final UUID actorUUID;
    private final ItemStack wand;
    private final int randomSeed;

    // Списки целей
    private final StoredValues storedValues;

    // Состояние стейт-машины
    private int lastIndex;
    private int waitTicksLeft;

    // Рантайм-переменные (не сериализуются)
    private int currentIndex;
    private ServerLevel level;
    private Entity actor;
    private boolean initialized = false;
    private RandomSource randomSource;

    // Конструктор создания (при первом касте)
    public SpellRunner(Holder<SpellAction> action, Entity actor, ItemStack wand) {
        this.action = action;
        this.wand = wand;
        this.actor = actor;
        this.actorUUID = actor.getUUID();
        this.level = (ServerLevel) actor.level();
        this.lastIndex = 0;
        this.waitTicksLeft = 0;
        this.initialized = true;
        this.randomSource = level.getRandom().fork();
        this.randomSeed = randomSource.nextInt();
        this.storedValues = new StoredValues();
    }

    // Десериализационный конструктор для Codec
    private SpellRunner(Holder<SpellAction> action, UUID actorUUID, ItemStack wand,
                        int randomSeed, int lastIndex, int waitTicksLeft, StoredValues storedValues) {
        this.action = action;
        this.actorUUID = actorUUID;
        this.wand = wand;
        this.randomSeed = randomSeed;
        this.lastIndex = lastIndex;
        this.waitTicksLeft = waitTicksLeft;
        this.storedValues = storedValues;
        // Поля уровня и сущностей инициализируются позже в методе init() при загрузке мира
    }

    /**
     * Важный метод! Вызывается менеджером сразу после десериализации,
     * чтобы восстановить живые ссылки на Entity и ServerLevel по их UUID.
     */
    public void init(ServerLevel level) {
        this.level = level;
        this.actor = level.getEntity(this.actorUUID);
        this.randomSource = level.getRandom().fork();

        storedValues.init(level);
        this.initialized = true;
    }

    /**
     * Тик ранера.
     * @return true, если заклинание ПОЛНОСТЬЮ завершило работу (успех или провал) и ранер можно удалять.
     */
    public boolean tick() {
        if (!initialized || level == null || actor == null) {
            // Если мир загрузился, а кастер исчез/умер — аварийно завершаем спелл
            return true;
        }

        if (waitTicksLeft > 0) {
            waitTicksLeft--;
            return false; // Спелл еще активен, ждем
        }

        currentIndex = 0;

        // Берем корневое действие из холдера нашего Spell
        ProcessStatus status = action.value().run(this);

        // Если статус не RUNNING, значит спелл отработал до конца
        return status != ProcessStatus.RUNNING;
    }

    private boolean shouldExecute() {
        int step = currentIndex;
        currentIndex++;
        if (step < lastIndex) {
            return false;
        }
        lastIndex++;
        return true;
    }

    public ProcessStatus execute(Predicate<ServerLevel> action) {
        if (shouldExecute()) return action.test(level) ? ProcessStatus.SUCCESS : ProcessStatus.FAILURE;
        return ProcessStatus.SUCCESS;
    }

    public ProcessStatus executeWait(int ticks) {
        if (shouldExecute()) {
            this.waitTicksLeft = ticks;
            return ProcessStatus.RUNNING;
        }
        return ProcessStatus.SUCCESS;
    }

    public <T> ProcessStatus executeFor(StoredValueType<T> valueType, BiPredicate<ServerLevel, T> action) {
        return getOptional(valueType).filter(value -> action.test(level, value)).map(_ -> ProcessStatus.SUCCESS).orElse(ProcessStatus.FAILURE);
    }

    public ProcessStatus executeForAnyBlock(BlockTargetPredicate action) {
        boolean check = false;
        for (BlockPos blockTarget : new ArrayList<>(getBlockTargets())) {
            check |= action.test(level, blockTarget, level.getBlockState(blockTarget));
        }
        return check ? ProcessStatus.SUCCESS : ProcessStatus.FAILURE;
    }

    public ProcessStatus executeForAnyEntity(BiPredicate<ServerLevel, Entity> action) {

        boolean check = false;
        for (EntityHolder entity : new ArrayList<>(getOrCreateDefault(StoredValueType.ENTITIES))) {
            check |= action.test(level, entity.getEntity());
        }
        return check ? ProcessStatus.SUCCESS : ProcessStatus.FAILURE;
    }

    // Геттеры для работы логики действий заклинаний
    public List<BlockPos> getBlockTargets() { return getOrCreateDefault(StoredValueType.BLOCKS); }
    public List<Entity> getEntityTargets() { return getOrCreateDefault(StoredValueType.ENTITIES).stream().map(EntityHolder::getEntity).toList(); }
    public List<Vec3> getPositionTargets() { return getOrCreateDefault(StoredValueType.POSITIONS); }


    public RandomSource getRandom() {
        randomSource.setSeed(currentIndex + randomSeed);
        return randomSource;
    }

    public Entity caster() {
        return actor instanceof SpellProjectile projectile ? projectile.getOwner() : actor;
    }

    public List<Pair<BlockPos, BlockState>> getBlockTargetStates() {
        return getOptional(StoredValueType.BLOCKS).map(list -> list.stream().map(blockPos -> new Pair<>(blockPos, level.getBlockState(blockPos))).toList()).orElse(List.of());
    }

    public void replaceEntityTargets(Collection<Entity> entities) {
        put(StoredValueType.ENTITIES, new ArrayList<>(entities.stream().map(EntityHolder::new).toList()));
    }

    public void addBlockTargets(Collection<BlockPos> blockPos) {
        getOrCreateDefault(StoredValueType.BLOCKS).addAll(blockPos);
    }

    public void addEntityTarget(Entity entity) {
        getOrCreateDefault(StoredValueType.ENTITIES).add(new EntityHolder(entity));
    }

    public void addPositionTarget(Vec3 position) {
        getOrCreateDefault(StoredValueType.POSITIONS).add(position);
    }

    public void targetEntityByHit(@NotNull EntityHitResult hitResult) {
        put(StoredValueType.HIT, HitHolder.byHit(hitResult));
    }

    public void targetBlockByHit(@NotNull BlockHitResult blockHitResult) {
        put(StoredValueType.HIT, HitHolder.byHit(blockHitResult));
    }

    public <T> void put(StoredValueType<T> type, T value) {
        storedValues.put(type, value);
    }

    public <T> void putIfAbsent(StoredValueType<T> type, T value) {
        storedValues.putIfAbsent(type, value);
    }

    public <T> T get(StoredValueType<T> type) {
        return storedValues.get(type);
    }

    public <T> T getOrCreateDefault(StoredValueType<T> type) {
        return storedValues.getOrCreateDefault(type);
    }

    public <T> Optional<T> getOptional(StoredValueType<T> type) {
        return storedValues.getOptional(type);
    }

    public Entity getActor() {
        return actor;
    }

    public ItemStack getWand() {
        return wand;
    }


    public interface BlockTargetPredicate {
        boolean test(ServerLevel level, BlockPos blockPos, BlockState blockState);
    }


    public enum ProcessStatus {
        SUCCESS, FAILURE, RUNNING
    }


    public enum StatusHandle implements StringRepresentable {
        WHILE_SUCCESS("while_success"), WHILE_FAILURE("while_failure"), IGNORE("ignore");
        public static final Codec<StatusHandle> CODEC = StringRepresentable.fromEnum(StatusHandle::values);

        private final String name;

        StatusHandle(String name) {
            this.name = name;
        }

        public ProcessStatus handle(ProcessStatus status) {
            switch (this) {
                case WHILE_SUCCESS -> {
                    if (status == ProcessStatus.FAILURE) return ProcessStatus.FAILURE;
                    return ProcessStatus.RUNNING;
                }
                case WHILE_FAILURE -> {
                    if (status == ProcessStatus.SUCCESS) return ProcessStatus.SUCCESS;
                    return ProcessStatus.RUNNING;
                }
                case IGNORE -> {
                    return ProcessStatus.RUNNING;
                }
                default -> throw new IllegalStateException("Unexpected value: " + this);
            }
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

}
