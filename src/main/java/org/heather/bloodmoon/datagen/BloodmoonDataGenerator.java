package org.heather.bloodmoon.datagen;

import org.heather.bloodmoon.datagen.lang.BloodmoonEnglishLanguageProvider;
import org.heather.bloodmoon.datagen.lang.BloodmoonSpanishLanguageProvider;
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
