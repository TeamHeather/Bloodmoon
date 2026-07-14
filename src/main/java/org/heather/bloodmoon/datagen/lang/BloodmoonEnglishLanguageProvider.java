package org.heather.bloodmoon.datagen.lang;

import org.heather.bloodmoon.command.BloodmoonCommand;
import org.heather.bloodmoon.api.Bloodmoon;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class BloodmoonEnglishLanguageProvider extends FabricLanguageProvider {
    public BloodmoonEnglishLanguageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registryLookup, TranslationBuilder builder) {
        builder.add(Bloodmoon.BLOODMOON_REMAINING, "The Blood Moon ends in %s.");

        builder.add(BloodmoonCommand.ALREADY_ACTIVE, "The Blood Moon is already active.");
        builder.add(BloodmoonCommand.INACTIVE, "The Blood Moon is not currently active.");
        builder.add(BloodmoonCommand.START_SUCCESS, "The Blood Moon has begun and will last %s ticks.");
        builder.add(BloodmoonCommand.STOP_SUCCESS, "The Blood Moon has been stopped.");
        builder.add(BloodmoonCommand.GET_SUCCESS, "The Blood Moon will remain active for %s more ticks.");
        builder.add(BloodmoonCommand.SET_SUCCESS, "The Blood Moon's remaining duration has been set to %s ticks.");
        builder.add(BloodmoonCommand.CHANGE_SUCCESS, "The Blood Moon's remaining duration has been adjusted by %s ticks.");

    }
}