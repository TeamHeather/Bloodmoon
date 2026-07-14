package io.github.pepe3012.bloodmoon.datagen;

import io.github.pepe3012.bloodmoon.datagen.lang.BloodmoonEnglishLanguageProvider;
import io.github.pepe3012.bloodmoon.datagen.lang.BloodmoonSpanishLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public final class BloodmoonDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BloodmoonSpanishLanguageProvider::new);
        pack.addProvider(BloodmoonEnglishLanguageProvider::new);
    }
}
