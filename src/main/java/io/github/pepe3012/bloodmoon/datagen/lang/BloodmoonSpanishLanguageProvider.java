package io.github.pepe3012.bloodmoon.datagen.lang;

import io.github.pepe3012.bloodmoon.command.BloodmoonCommand;
import io.github.pepe3012.bloodmoon.api.Bloodmoon;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class BloodmoonSpanishLanguageProvider extends FabricLanguageProvider {
    public BloodmoonSpanishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, "es_es", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registryLookup, TranslationBuilder builder) {
        builder.add(Bloodmoon.BLOODMOON_REMAINING, "La Luna de Sangre termina en %s.");

        builder.add(BloodmoonCommand.ALREADY_ACTIVE, "La Luna de Sangre ya está activa.");
        builder.add(BloodmoonCommand.INACTIVE, "La Luna de Sangre no está activa.");
        builder.add(BloodmoonCommand.START_SUCCESS, "La Luna de Sangre ha comenzado y durará %s ticks.");
        builder.add(BloodmoonCommand.STOP_SUCCESS, "La Luna de Sangre ha sido detenida.");
        builder.add(BloodmoonCommand.GET_SUCCESS, "A la Luna de Sangre le quedan %s ticks.");
        builder.add(BloodmoonCommand.SET_SUCCESS, "La duración restante de la Luna de Sangre se estableció en %s ticks.");
        builder.add(BloodmoonCommand.CHANGE_SUCCESS, "La duración restante de la Luna de Sangre se modificó en %s ticks.");

    }
}