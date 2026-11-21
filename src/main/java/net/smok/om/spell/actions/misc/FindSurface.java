package net.smok.om.spell.actions.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.phys.Vec3;
import net.smok.om.spell.SpellRunner;
import net.smok.om.spell.actions.SpellAction;
import net.smok.om.spell.actions.SpellActionType;
import net.smok.om.utils.HitHolder;
import net.smok.om.utils.StoredValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record FindSurface(float range, String target, String surface, BlockPredicate filter) implements SpellAction {

    public static final MapCodec<FindSurface> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("range").forGetter(FindSurface::range),
            Codec.STRING.fieldOf("target").forGetter(FindSurface::target),
            Codec.STRING.fieldOf("surface").forGetter(FindSurface::surface),
            BlockPredicate.CODEC.optionalFieldOf("filter", BlockPredicate.solid()).forGetter(FindSurface::filter)
    ).apply(instance, FindSurface::new));

    @Contract("_, _, _, _ -> new")
    public static @NotNull Holder<SpellAction> create(float range, @NotNull String target, @NotNull String surface, @NotNull BlockPredicate filter) {
        return new FindSurface(range, target, surface, filter).inline();
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Holder<SpellAction> create(float range, @NotNull String target, @NotNull BlockPredicate filter) {
        return new FindSurface(range, target, "surface", filter).inline();
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Holder<SpellAction> create(float range, @NotNull String target, @NotNull String surface) {
        return new FindSurface(range, target, surface, BlockPredicate.solid()).inline();
    }

    @Contract("_, _ -> new")
    public static @NotNull Holder<SpellAction> create(float range, @NotNull String target) {
        return new FindSurface(range, target, "surface", BlockPredicate.solid()).inline();
    }

    @Override
    public SpellActionType<?> actionType() {
        return SpellActionType.FIND_SURFACE;
    }

    @Override
    public SpellRunner.ProcessStatus run(SpellRunner spellRunner) {
        return spellRunner.executeFor(target, StoredValueType.POSITION, (level, vec3) -> {
            BlockPos.MutableBlockPos pos = BlockPos.containing(vec3).mutable();
            for (int i = 0; i < Math.ceil(range); i++) {
                pos.move(0, -1, 0);
                if (filter.test(level, pos) && vec3.y - pos.getY() - 1 < range) {
                    spellRunner.put(surface, StoredValueType.HIT, new HitHolder(new Vec3(vec3.x, pos.getY() + 1, vec3.z), pos.immutable(), null, Direction.UP));
                    return true;
                }
            }
            return false;
        });
    }
}
