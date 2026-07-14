package io.github.pepe3012.bloodmoon.api;

import io.github.pepe3012.bloodmoon.BloodmoonMod;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v8.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v8.level.LevelComponentInitializer;

@SuppressWarnings("UnstableApiUsage")
public final class BloodmoonComponent implements AutoSyncedComponent, LevelComponentInitializer {
    public static final ComponentKey<BloodmoonComponent> KEY = ComponentRegistry.getOrCreate(BloodmoonMod.identifier("base"), BloodmoonComponent.class);

    private int totalTicks;
    private int remainingTicks;
    private boolean active;

    void setTotalTicks(int totalTicks) {
        this.totalTicks = totalTicks;
    }

    int getTotalTicks() {
        return this.totalTicks;
    }

    void setRemainingTicks(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    int getRemainingTicks() {
        return this.remainingTicks;
    }

    void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.registerFor(Level.OVERWORLD, KEY, _ -> new BloodmoonComponent());
    }

    @Override
    public void readData(@NonNull ValueInput input) {
        this.totalTicks = input.getIntOr("TotalTicks", this.totalTicks);
        this.remainingTicks = input.getIntOr("RemainingTicks", this.remainingTicks);
        this.active = input.getBooleanOr("Active", this.active);
    }

    @Override
    public void writeData(@NonNull ValueOutput output) {
        output.putInt("TotalTicks", this.totalTicks);
        output.putInt("RemainingTicks", this.remainingTicks);
        output.putBoolean("Active", this.active);
    }
}