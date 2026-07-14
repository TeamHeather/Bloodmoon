package io.github.pepe3012.bloodmoon;

import io.github.pepe3012.bloodmoon.command.BloodmoonCommand;
import io.github.pepe3012.bloodmoon.api.Bloodmoon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BloodmoonMod implements ModInitializer {
    public static final String MOD_ID = "bloodmoon";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Bloodmoon mod...");
        ServerLifecycleEvents.SERVER_STARTED.register(Bloodmoon::initialize);
        CommandRegistrationCallback.EVENT.register(new BloodmoonCommand());
        LOGGER.info("Bloodmoon mod initialized.");
    }

    public static Identifier identifier(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}