package net.smok.om.spell;


import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.smok.om.entity.SpellProjectile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
            BlockPos.CODEC.listOf().fieldOf("block_targets").forGetter(r -> r.blockTargets),
            UUIDUtil.CODEC.listOf().fieldOf("entity_target_uuids").forGetter(r -> r.entityTargetUUIDs)
    ).apply(instance, SpellRunner::new));

    // Константные данные каста
    private final Holder<SpellAction> action;
    private final UUID actorUUID;
    private final ItemStack wand;
    private final int randomSeed;

    // Списки целей
    private final List<BlockPos> blockTargets;
    private final List<UUID> entityTargetUUIDs; // Сохраняем UUID существ
    private final List<Entity> entityTargets = new ArrayList<>(); // Рантайм-кэш существ

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
        this.blockTargets = new ArrayList<>();
        this.entityTargetUUIDs = new ArrayList<>();
        this.lastIndex = 0;
        this.waitTicksLeft = 0;
        this.initialized = true;
        this.randomSource = level.getRandom().fork();
        this.randomSeed = randomSource.nextInt();
    }

    // Десериализационный конструктор для Codec
    private SpellRunner(Holder<SpellAction> action, UUID actorUUID, ItemStack wand,
                        int randomSeed, int lastIndex, int waitTicksLeft, List<BlockPos> blockTargets, List<UUID> entityTargetUUIDs) {
        this.action = action;
        this.actorUUID = actorUUID;
        this.wand = wand;
        this.randomSeed = randomSeed;
        this.lastIndex = lastIndex;
        this.waitTicksLeft = waitTicksLeft;
        this.blockTargets = new ArrayList<>(blockTargets);
        this.entityTargetUUIDs = new ArrayList<>(entityTargetUUIDs);
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

        this.entityTargets.clear();
        for (UUID uuid : this.entityTargetUUIDs) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) {
                this.entityTargets.add(entity);
            }
        }
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

    public ProcessStatus executeForAnyBlock(BlockTargetPredicate action) {
        boolean check = false;
        for (BlockPos blockTarget : new ArrayList<>(blockTargets)) {
            check |= action.test(level, blockTarget, level.getBlockState(blockTarget));
        }
        return check ? ProcessStatus.SUCCESS : ProcessStatus.FAILURE;
    }

    public ProcessStatus executeForAnyEntity(BiPredicate<ServerLevel, Entity> action) {

        boolean check = false;
        for (Entity entity : new ArrayList<>(entityTargets)) {
            check |= action.test(level, entity);
        }
        return check ? ProcessStatus.SUCCESS : ProcessStatus.FAILURE;
    }

    // Геттеры для работы логики действий заклинаний
    public List<BlockPos> getBlockTargets() { return blockTargets; }
    public List<Entity> getEntityTargets() { return entityTargets; }


    public RandomSource getRandom() {
        randomSource.setSeed(currentIndex + randomSeed);
        return randomSource;
    }

    public Entity caster() {
        return actor instanceof SpellProjectile projectile ? projectile.getOwner() : actor;
    }

    public List<Pair<BlockPos, BlockState>> getBlockTargetStates() {
        return blockTargets.stream().map(blockPos -> new Pair<>(blockPos, level.getBlockState(blockPos))).toList();
    }

    public ProcessStatus replaceEntityTargets(Collection<Entity> entities) {
        entityTargets.clear();
        entityTargetUUIDs.clear();

        if (entities.isEmpty()) return ProcessStatus.FAILURE;

        entityTargets.addAll(entities);
        entityTargetUUIDs.addAll(entities.stream().map(Entity::getUUID).toList());

        return ProcessStatus.SUCCESS;
    }

    public ProcessStatus replaceBlockTargets(Collection<BlockPos> blockPoses) {
        blockTargets.clear();

        if (blockPoses.isEmpty()) return ProcessStatus.FAILURE;

        blockTargets.addAll(blockPoses);
        return ProcessStatus.SUCCESS;
    }

    public void addBlockTargets(Collection<BlockPos> blockPos) {
        blockTargets.addAll(blockPos);
    }

    public void addEntityTarget(Entity entity) {
        if (!this.entityTargetUUIDs.contains(entity.getUUID())) {
            this.entityTargetUUIDs.add(entity.getUUID());
            this.entityTargets.add(entity);
        }
    }

    public void targetEntityByHit(@NotNull EntityHitResult hitResult) {
        addEntityTarget(hitResult.getEntity());
    }

    public void targetBlockByHit(@NotNull BlockHitResult blockHitResult) {
        blockTargets.add(blockHitResult.getBlockPos());
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

}
