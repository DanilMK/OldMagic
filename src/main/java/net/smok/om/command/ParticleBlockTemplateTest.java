package net.smok.om.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.smok.om.utils.BlockParticleTemplate;

public class ParticleBlockTemplateTest implements CommandRegistrationCallback {

    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.particle.failed"));

    @Override
    public void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        commandDispatcher.register(Commands.literal("spellParticleBlockTemplate")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.argument("name", ParticleArgument.particle(commandBuildContext))
                .then(Commands.argument("position", Vec3Argument.vec3())
                    .then(Commands.argument("size", Vec3Argument.vec3())
                        .then(Commands.argument("count", IntegerArgumentType.integer(0))
                            .then(Commands.literal("solid")
                                .then(Commands.literal("static")
                                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                            .executes(commandContext ->
                                                sendParticles(
                                                    commandContext.getSource(),
                                                    ParticleArgument.getParticle(commandContext, "name"),
                                                    Vec3Argument.getVec3(commandContext, "position"),
                                                    Vec3Argument.getVec3(commandContext, "size"),
                                                    IntegerArgumentType.getInteger(commandContext, "count"),
                                                    BlockParticleTemplate.SOLID_APPEARING,
                                                    BlockParticleTemplate.MoveRule.STATIC,
                                                    DoubleArgumentType.getDouble(commandContext, "speed"),
                                                    DoubleArgumentType.getDouble(commandContext, "randomness")
                                                )
                                            )
                                        )
                                    )
                                ).then(Commands.literal("from_center")
                                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                            .executes(commandContext ->
                                                sendParticles(
                                                    commandContext.getSource(),
                                                    ParticleArgument.getParticle(commandContext, "name"),
                                                    Vec3Argument.getVec3(commandContext, "position"),
                                                    Vec3Argument.getVec3(commandContext, "size"),
                                                    IntegerArgumentType.getInteger(commandContext, "count"),
                                                    BlockParticleTemplate.SOLID_APPEARING,
                                                    BlockParticleTemplate.MoveRule.FROM_CENTER,
                                                    DoubleArgumentType.getDouble(commandContext, "speed"),
                                                    DoubleArgumentType.getDouble(commandContext, "randomness")
                                                )
                                            )
                                        )
                                    )
                                ).then(Commands.literal("to_center")
                                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                            .executes(commandContext ->
                                                sendParticles(
                                                    commandContext.getSource(),
                                                    ParticleArgument.getParticle(commandContext, "name"),
                                                    Vec3Argument.getVec3(commandContext, "position"),
                                                    Vec3Argument.getVec3(commandContext, "size"),
                                                    IntegerArgumentType.getInteger(commandContext, "count"),
                                                    BlockParticleTemplate.SOLID_APPEARING,
                                                    BlockParticleTemplate.MoveRule.TO_CENTER,
                                                    DoubleArgumentType.getDouble(commandContext, "speed"),
                                                    DoubleArgumentType.getDouble(commandContext, "randomness")
                                                )
                                            )
                                        )
                                    )
                                ).then(Commands.literal("vortex")
                                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                        .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                            .executes(commandContext ->
                                                sendParticles(
                                                    commandContext.getSource(),
                                                    ParticleArgument.getParticle(commandContext, "name"),
                                                    Vec3Argument.getVec3(commandContext, "position"),
                                                    Vec3Argument.getVec3(commandContext, "size"),
                                                    IntegerArgumentType.getInteger(commandContext, "count"),
                                                    BlockParticleTemplate.SOLID_APPEARING,
                                                    BlockParticleTemplate.MoveRule.VORTEX,
                                                    DoubleArgumentType.getDouble(commandContext, "speed"),
                                                    DoubleArgumentType.getDouble(commandContext, "randomness")
                                                )
                                            )
                                        )
                                    )
                                )
                            ).then(Commands.literal("faces")
                                .then(Commands.argument("thickness", FloatArgumentType.floatArg(0, 1))
                                    .then(Commands.literal("static")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.FacesAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.STATIC,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    ).then(Commands.literal("from_center")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.FacesAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.FROM_CENTER,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    ).then(Commands.literal("to_center")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.FacesAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.TO_CENTER,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    ).then(Commands.literal("vortex")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.FacesAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.VORTEX,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            ).then(Commands.literal("outline")
                                .then(Commands.argument("thickness", FloatArgumentType.floatArg(0, 1))
                                    .then(Commands.literal("static")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.OutlineAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.STATIC,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    ).then(Commands.literal("from_center")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.OutlineAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.FROM_CENTER,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    ).then(Commands.literal("to_center")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.OutlineAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.TO_CENTER,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    ).then(Commands.literal("vortex")
                                        .then(Commands.argument("speed", DoubleArgumentType.doubleArg())
                                            .then(Commands.argument("randomness", DoubleArgumentType.doubleArg())
                                                .executes(commandContext ->
                                                    sendParticles(
                                                            commandContext.getSource(),
                                                            ParticleArgument.getParticle(commandContext, "name"),
                                                            Vec3Argument.getVec3(commandContext, "position"),
                                                            Vec3Argument.getVec3(commandContext, "size"),
                                                            IntegerArgumentType.getInteger(commandContext, "count"),
                                                            new BlockParticleTemplate.OutlineAppearing(FloatArgumentType.getFloat(commandContext, "thickness")),
                                                            BlockParticleTemplate.MoveRule.VORTEX,
                                                            DoubleArgumentType.getDouble(commandContext, "speed"),
                                                            DoubleArgumentType.getDouble(commandContext, "randomness")
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    private static int sendParticles(
            CommandSourceStack source,
            ParticleOptions particle,
            Vec3 position,
            Vec3 size,
            int count,
            BlockParticleTemplate.Appearing appearing,
            BlockParticleTemplate.MoveRule moveRule,
            double speed,
            double randomness
            ) throws CommandSyntaxException {
        BlockParticleTemplate template = new BlockParticleTemplate(particle, position, size, count, appearing, new BlockParticleTemplate.Movement(moveRule, speed, randomness));
        int result = 0;
        for (ServerPlayer player : source.getLevel().players()) {
            if (template.addParticlesServer(source.getLevel(), player, Vec3.ZERO)) {
                result++;
            }
        }

        if (result == 0) {
            throw ERROR_FAILED.create();
        }

        source.sendSuccess(() -> Component.translatable("commands.particle.success", BuiltInRegistries.PARTICLE_TYPE.getKey(particle.getType()).toString()), true);
        return result;
    }
}
