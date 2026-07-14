package org.heather.bloodmoon.api;

import org.heather.bloodmoon.BloodmoonMod;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v8.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v8.level.LevelComponentInitializer;

/**
 * Stores the persistent and synchronized state of a bloodmoon for a level.
 *
 * <p>The component tracks whether a bloodmoon is active, its original duration,
 * and its remaining duration. Its data is attached exclusively to the overworld
 * and is automatically synchronized through Cardinal Components API.</p>
 *
 * <p>State changes are managed by {@link Bloodmoon}. Callers outside this package
 * can query the active state through {@link #isActive()}.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class BloodmoonComponent implements AutoSyncedComponent, LevelComponentInitializer {

    /**
     * The component key used to retrieve and synchronize the bloodmoon component
     * associated with a level.
     */
    public static final ComponentKey<BloodmoonComponent> KEY = ComponentRegistry.getOrCreate(
            BloodmoonMod.identifier("base"),
            BloodmoonComponent.class
    );

    private int totalTicks;
    private int remainingTicks;
    private boolean active;

    /**
     * Sets the original duration of the current bloodmoon.
     *
     * @param totalTicks the original duration in ticks
     */
    void setTotalTicks(int totalTicks) {
        this.totalTicks = totalTicks;
    }

    /**
     * Returns the original duration of the current bloodmoon.
     *
     * @return the original duration in ticks
     */
    int getTotalTicks() {
        return this.totalTicks;
    }

    /**
     * Sets the remaining duration of the current bloodmoon.
     *
     * @param remainingTicks the remaining duration in ticks
     */
    void setRemainingTicks(int remainingTicks) {
        this.remainingTicks = remainingTicks;
    }

    /**
     * Returns the remaining duration of the current bloodmoon.
     *
     * @return the remaining duration in ticks
     */
    int getRemainingTicks() {
        return this.remainingTicks;
    }

    /**
     * Sets whether a bloodmoon is active.
     *
     * @param active {@code true} to mark the bloodmoon as active; otherwise
     *               {@code false}
     */
    void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Determines whether a bloodmoon is currently active.
     *
     * @return {@code true} if a bloodmoon is active; otherwise {@code false}
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Registers the bloodmoon component factory for the overworld.
     *
     * @param registry the level component factory registry
     */
    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.registerFor(Level.OVERWORLD, KEY, _ -> new BloodmoonComponent());
    }

    /**
     * Restores the bloodmoon state from persistent level data.
     *
     * <p>Missing values retain their current defaults to support data created
     * before a field was introduced.</p>
     *
     * @param input the serialized component data
     */
    @Override
    public void readData(@NonNull ValueInput input) {
        this.totalTicks = input.getIntOr("TotalTicks", this.totalTicks);
        this.remainingTicks = input.getIntOr("RemainingTicks", this.remainingTicks);
        this.active = input.getBooleanOr("Active", this.active);
    }

    /**
     * Writes the current bloodmoon state to persistent level data.
     *
     * @param output the destination for the serialized component data
     */
    @Override
    public void writeData(@NonNull ValueOutput output) {
        output.putInt("TotalTicks", this.totalTicks);
        output.putInt("RemainingTicks", this.remainingTicks);
        output.putBoolean("Active", this.active);
    }
}