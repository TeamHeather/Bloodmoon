package io.github.pepe3012.bloodmoon.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.pepe3012.bloodmoon.api.Bloodmoon;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.jspecify.annotations.NonNull;

public final class BloodmoonCommand implements CommandRegistrationCallback {

    public static final String ALREADY_ACTIVE = "commands.bloodmoon.already_active";
    public static final String INACTIVE = "commands.bloodmoon.inactive";
    public static final String START_SUCCESS = "commands.bloodmoon.start.success";
    public static final String STOP_SUCCESS = "commands.bloodmoon.stop.success";
    public static final String GET_SUCCESS = "commands.bloodmoon.get.success";
    public static final String SET_SUCCESS = "commands.bloodmoon.set.success";
    public static final String CHANGE_SUCCESS = "commands.bloodmoon.change.success";

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
        if (bloodmoon.isBloodmoonActive()) return fail(context, ALREADY_ACTIVE);

        int duration = IntegerArgumentType.getInteger(context, DURATION_ARGUMENT);
        bloodmoon.startBloodmoon(duration);
        return success(context, START_SUCCESS, duration);
    }

    private static int stop(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return fail(context, INACTIVE);

        bloodmoon.stopBloodmoon();
        return success(context, STOP_SUCCESS);
    }

    private static int get(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return success(context, INACTIVE);

        return success(context, GET_SUCCESS, bloodmoon.getBloodmoonRemainingTicks());
    }

    private static int set(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return fail(context, INACTIVE);

        int remainingTicks = IntegerArgumentType.getInteger(context, REMAINING_TICKS_ARGUMENT);
        bloodmoon.setBloodmoonRemainingTicks(remainingTicks);
        return success(context, SET_SUCCESS, remainingTicks);
    }

    private static int change(CommandContext<CommandSourceStack> context) {
        Bloodmoon bloodmoon = Bloodmoon.getInstance();
        if (!bloodmoon.isBloodmoonActive()) return fail(context, INACTIVE);

        int delta = IntegerArgumentType.getInteger(context, DELTA_ARGUMENT);
        bloodmoon.changeBloodmoonRemainingTicks(delta);
        return success(context, CHANGE_SUCCESS, delta);
    }

    private static int success(CommandContext<CommandSourceStack> context, String key, Object... arguments) {
        context.getSource().sendSuccess(() -> Component.translatable(key, arguments), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int fail(CommandContext<CommandSourceStack> context, String key, Object... arguments) {
        context.getSource().sendFailure(Component.translatable(key, arguments));
        return 0;
    }
}