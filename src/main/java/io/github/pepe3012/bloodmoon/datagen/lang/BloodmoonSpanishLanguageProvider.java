package io.github.pepe3012.bloodmoon.datagen.provider;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public final class BloodmoonLanguageProvider extends FabricLanguageProvider {
    public BloodmoonLanguageProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, "es_es", registryLookup);
    }

    @Override
    public void generateTranslations(@NonNull HolderLookup.Provider lookup, @NonNull TranslationBuilder builder) {
        builder.add("bossbar.bloodmoon.remaining", "Quedan %s para que finalice la Luna de Sangre.");
    }
}
