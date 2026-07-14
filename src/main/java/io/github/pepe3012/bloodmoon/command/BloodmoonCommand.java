package io.github.pepe3012.arcadia.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.pepe3012.arcadia.common.component.world.bloodmoon.Bloodmoon;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.jspecify.annotations.NonNull;

public class BloodmoonCommand implements CommandRegistrationCallback {
    private static final String DURATION_ARGUMENT = "duration";
    private static final String REMAINING_TICKS_ARGUMENT = "remainingTicks";
    private static final String DELTA_ARGUMENT = "delta";

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, @NonNull CommandBuildContext buildContext, Commands.@NonNull CommandSelection selection) {
        dispatcher.register(Commands.literal("bloodmoon")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                .then(Commands.literal("start")
                        .then(Commands.argument(DURATION_ARGUMENT, IntegerArgumentType.integer(1))
                                .executes(BloodmoonCommand::start)))
                .then(Commands.literal("stop")
                        .executes(BloodmoonCommand::stop))
                .then(Commands.literal("get")
                        .executes(BloodmoonCommand::get))
                .then(Commands.literal("set")
                        .then(Commands.argument(REMAINING_TICKS_ARGUMENT, IntegerArgumentType.integer(0))
                                .executes(BloodmoonCommand::set)))
                .then(Commands.literal("change")
                        .then(Commands.argument(DELTA_ARGUMENT, IntegerArgumentType.integer())
                                .executes(BloodmoonCommand::change))));
    }

    private static int start(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (bloodmoon.isBloodmoonActive()) return fail(context, "The blood moon is already active.");

        int duration = IntegerArgumentType.getInteger(context, DURATION_ARGUMENT);
        bloodmoon.startBloodmoon(duration);
        return success(context, "Started the blood moon for " + duration + " ticks.");
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return fail(context, "The blood moon is not active.");

        bloodmoon.stopBloodmoon();
        return success(context, "Stopped the blood moon.");
    }

    private static int get(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();

        if (!bloodmoon.isBloodmoonActive()) {
            return success(context, "The blood moon is not active.");
        }

        return success(context, "The blood moon has " + bloodmoon.getBloodmoonRemainingTicks() + " ticks remaining.");
    }

    private static int set(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return fail(context, "The blood moon is not active.");

        int remainingTicks = IntegerArgumentType.getInteger(context, REMAINING_TICKS_ARGUMENT);
        bloodmoon.setBloodmoonRemainingTicks(remainingTicks);
        return success(context, "Set the remaining blood moon duration to " + remainingTicks + " ticks.");
    }

    private static int change(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return fail(context, "The blood moon is not active.");

        int delta = IntegerArgumentType.getInteger(context, DELTA_ARGUMENT);
        bloodmoon.changeBloodmoonRemainingTicks(delta);
        return success(context, "Changed the blood moon duration by " + delta + " ticks.");
    }

    private static int success(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int fail(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(Component.literal(message));
        return 0;
    }
}