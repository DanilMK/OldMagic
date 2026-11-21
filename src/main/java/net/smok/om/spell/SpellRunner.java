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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.smok.om.entity.SpellProjectile;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.cast.PlayerCaster;
import net.smok.om.utils.EntityHolder;
import net.smok.om.utils.HitHolder;
import net.smok.om.utils.StoredValueType;
import net.smok.om.utils.StoredValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SpellRunner {

    // Статический кодек для полной сериализации ранера
    public static final Codec<SpellRunner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Spell.HOLDER_CODEC.optionalFieldOf("spell").forGetter(r -> Optional.ofNullable(r.spell)),
            SpellAction.HOLDER_CODEC.fieldOf("action").forGetter(r -> r.action),
            UUIDUtil.CODEC.fieldOf("actor_uuid").forGetter(r -> r.actorUUID),
            ItemStack.CODEC.fieldOf("wand").forGetter(r -> r.wand),
            Codec.INT.fieldOf("random_seed").forGetter(r -> r.randomSeed),
            Codec.INT.fieldOf("last_index").forGetter(r -> r.lastIndex),
            Codec.INT.fieldOf("wait_ticks_left").forGetter(r -> r.waitTicksLeft),
            StoredValues.CODEC.fieldOf("stored_values").forGetter(r -> r.storedValues)
    ).apply(instance, (spell, action1, actorUUID1, wand1, randomSeed1, lastIndex1, waitTicksLeft1, storedValues1) ->
            new SpellRunner(spell.orElse(null), action1, actorUUID1, wand1, randomSeed1, lastIndex1, waitTicksLeft1, storedValues1)));

    // Константные данные каста
    @Nullable
    private final Holder<Spell> spell;
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
    public SpellRunner(Holder<Spell> spell, Holder<SpellAction> action, Entity actor, ItemStack wand) {
        this.spell = spell;
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
        storedValues.put("actor", StoredValueType.ENTITY, new EntityHolder(actor));
        if (actor instanceof Projectile projectile) {
            storedValues.put("projectile", StoredValueType.ENTITY, new EntityHolder(projectile));
            if (projectile.getOwner() != null) storedValues.put("caster", StoredValueType.ENTITY, new EntityHolder(projectile.getOwner()));
        } else {
            if (actor instanceof PlayerCaster) storedValues.put("caster", StoredValueType.ENTITY, new EntityHolder(actor));
        }
    }

    // Десериализационный конструктор для Codec
    private SpellRunner(Holder<Spell> spell, Holder<SpellAction> action, UUID actorUUID, ItemStack wand,
                        int randomSeed, int lastIndex, int waitTicksLeft, StoredValues storedValues) {
        this.spell = spell;
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

    public boolean executeWait(int ticks) {
        if (shouldExecute() && ticks > 0) {
            this.waitTicksLeft = ticks;
            return true;
        }
        return false;
    }

    public <T> ProcessStatus executeFor(String key, StoredValueType<T> valueType, BiPredicate<ServerLevel, T> action) {
        if (shouldExecute()) return getOptional(key, valueType).filter(value -> action.test(level, value)).map(_ -> ProcessStatus.SUCCESS).orElse(ProcessStatus.FAILURE);
        return ProcessStatus.SUCCESS;
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
        for (EntityHolder entity : new ArrayList<>(getOrCreateList("entities", StoredValueType.ENTITY))) {
            check |= action.test(level, entity.getEntity());
        }
        return check ? ProcessStatus.SUCCESS : ProcessStatus.FAILURE;
    }

    // Геттеры для работы логики действий заклинаний
    public List<BlockPos> getBlockTargets() { return getOrCreateList("blocks", StoredValueType.BLOCK); }
    public List<Entity> getEntityTargets() { return getOrCreateList("entities", StoredValueType.ENTITY).stream().map(EntityHolder::getEntity).toList(); }
    public List<Vec3> getPositionTargets() { return getOrCreateList("positions", StoredValueType.POSITION); }


    public RandomSource getRandom() {
        randomSource.setSeed(currentIndex + randomSeed);
        return randomSource;
    }

    public Entity caster() {
        return actor instanceof SpellProjectile projectile ? projectile.getOwner() : actor;
    }

    public List<Pair<BlockPos, BlockState>> getBlockTargetStates() {
        return getOrCreateList("blocks", StoredValueType.BLOCK).stream().map(blockPos -> new Pair<>(blockPos, level.getBlockState(blockPos))).toList();
    }

    public void replaceEntityTargets(Collection<Entity> entities) {
        replaceList("entities", StoredValueType.ENTITY, new ArrayList<>(entities.stream().map(EntityHolder::new).toList()));
    }

    public void addBlockTargets(Collection<BlockPos> blockPos) {
        getOrCreateList("blocks", StoredValueType.BLOCK).addAll(blockPos);
    }

    public void addEntityTarget(Entity entity) {
        getOrCreateList("blocks", StoredValueType.ENTITY).add(new EntityHolder(entity));
    }

    public void addPositionTarget(Vec3 position) {
        getOrCreateList("positions", StoredValueType.POSITION).add(position);
    }

    public void targetByHit(@NotNull HitResult hitResult) {
        put("hit", StoredValueType.HIT, HitHolder.byHit(hitResult));
    }

    public <T> void put(String key, StoredValueType<T> type, T value) {
        storedValues.put(key, type, value);
    }

    public <T> void putIfAbsent(String key, StoredValueType<T> type, T value) {
        storedValues.putIfAbsent(key, type, value);
    }

    public void put(String setTargetName, StoredValueType.Value<?> value) {
        storedValues.put(setTargetName, value);
    }

    public <T> T get(String key, StoredValueType<T> type) {
        return storedValues.get(key, type);
    }

    public <T> T getOrCreateDefault(String key, StoredValueType<T> type) {
        return storedValues.getOrCreateDefault(key, type);
    }

    public <T> Optional<T> getOptional(String key, StoredValueType<T> type) {
        return storedValues.getOptional(key, type);
    }

    @Nullable
    public StoredValueType.Value<?> getAny(@NotNull String key) {
        return storedValues.getAny(key);
    }

    public <T> List<T> getOrCreateList(String key, StoredValueType<T> type) {
        return storedValues.getOrCreateList(key, type);
    }

    public <T> void replaceList(String key, StoredValueType<T> type, ArrayList<T> list) {
        storedValues.replaceList(key, type, list);
    }

    public void replaceList(String key, StoredValueType.ListValue<?> list) {
        storedValues.replaceList(key, list);
    }

    @Nullable
    public StoredValueType.ListValue<?> getAnyList(@NotNull String key) {
        return storedValues.getAnyList(key);
    }

    public Entity getActor() {
        return actor;
    }

    public ItemStack getWand() {
        return wand;
    }

    public String info() {
        return "Action: " + action + ", Actor: " + actor + ", Values: Singles: " + storedValues.singlesSize() + ", Lists: " + storedValues.listsSize();
    }

    public @Nullable Holder<Spell> getSpell() {
        return spell;
    }

    public @NotNull StoredValues getStoredValues() {
        return storedValues;
    }

    public void setStoredValues(@NotNull StoredValues values) {
        values.init(level);
        storedValues.putAll(values);
    }


    public interface BlockTargetPredicate {
        boolean test(ServerLevel level, BlockPos blockPos, BlockState blockState);
    }


    public enum ProcessStatus {
        SUCCESS, FAILURE, RUNNING
    }


    public enum StatusHandle implements StringRepresentable {
        CHAIN("chain", ProcessStatus.SUCCESS),
        FIRST_ENTER("first_enter", ProcessStatus.FAILURE),
        IGNORE("ignore", ProcessStatus.SUCCESS);

        public static final Codec<StatusHandle> CODEC = StringRepresentable.fromEnum(StatusHandle::values);

        private final String name;
        private final ProcessStatus onEnd;

        StatusHandle(String name, ProcessStatus onEnd) {
            this.name = name;
            this.onEnd = onEnd;
        }

        public ProcessStatus handle(ProcessStatus status) {
            switch (this) {
                case CHAIN -> {
                    if (status == ProcessStatus.FAILURE) return ProcessStatus.FAILURE;
                    return ProcessStatus.RUNNING;
                }
                case FIRST_ENTER -> {
                    if (status == ProcessStatus.SUCCESS) return ProcessStatus.SUCCESS;
                    return ProcessStatus.RUNNING;
                }
                case IGNORE -> {
                    return ProcessStatus.RUNNING;
                }
                default -> throw new IllegalStateException("Unexpected value: " + this);
            }
        }

        public ProcessStatus getOnEnd() {
            return onEnd;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

}
