package org.heather.bloodmoon.api;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Manages the server-wide bloodmoon state, duration, boss bar, persistence,
 * synchronization, and lifecycle events.
 *
 * <p>A single instance is associated with the currently running server. The bloodmoon
 * state is stored in the overworld's {@link BloodmoonComponent} and synchronized with
 * connected clients when it changes.</p>
 *
 * <p>This class must be initialized through {@link #initialize(MinecraftServer)}
 * before {@link #getInstance()} is used.</p>
 */
public final class Bloodmoon {

    /**
     * The RGB color applied to the moon while a bloodmoon is active.
     */
    public static final Vector3fc MOON_COLOR = new Vector3f(0.65F, 0.02F, 0.02F);

    /**
     * The translation key used for the bloodmoon remaining-time boss bar.
     */
    public static final String BLOODMOON_REMAINING = "bossbar.bloodmoon.remaining";

    private static Bloodmoon instance;
    private static boolean eventsRegistered;

    private final MinecraftServer minecraftServer;
    private final ServerLevel overworld;
    private final ServerBossEvent bossBar;
    private final BloodmoonComponent component;

    private Bloodmoon(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.overworld = minecraftServer.overworld();
        this.bossBar = new ServerBossEvent(UUID.randomUUID(), Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        this.component = BloodmoonComponent.KEY.get(this.overworld);

        this.configureBossBar();

        if (this.isBloodmoonActive()) this.restoreBloodmoon();
    }

    /**
     * Initializes the bloodmoon manager for the specified server and registers its
     * lifecycle event listeners.
     *
     * <p>Calling this method replaces the current bloodmoon instance. Event listeners
     * are registered only once.</p>
     *
     * @param minecraftServer the server associated with the bloodmoon manager
     * @throws NullPointerException if {@code minecraftServer} is {@code null}
     */
    public static void initialize(MinecraftServer minecraftServer) {
        instance = new Bloodmoon(Objects.requireNonNull(minecraftServer, "minecraftServer"));
        registerEvents();
    }

    /**
     * Creates the translated boss bar name containing the remaining bloodmoon time.
     *
     * @param remainingTime the formatted remaining time
     * @return the translated boss bar component
     */
    public static Component getBossbarName(String remainingTime) {
        return Component.translatable(BLOODMOON_REMAINING, remainingTime);
    }

    /**
     * Returns the bloodmoon manager associated with the current server.
     *
     * @return the initialized bloodmoon manager
     * @throws IllegalStateException if the manager has not been initialized
     */
    public static Bloodmoon getInstance() {
        if (instance == null) throw new IllegalStateException("Bloodmoon has not been initialized.");
        return instance;
    }

    /**
     * Determines whether a bloodmoon is active in the specified level.
     *
     * @param level the level whose bloodmoon component is queried
     * @return {@code true} if a bloodmoon is active; otherwise {@code false}
     * @throws NullPointerException if {@code level} is {@code null}
     */
    public static boolean isBloodmoonActive(Level level) {
        Objects.requireNonNull(level, "level");
        return BloodmoonComponent.KEY.get(level).isActive();
    }

    /**
     * Starts a bloodmoon with the specified duration.
     *
     * <p>The bloodmoon state is synchronized, the boss bar is shown to all connected
     * players, and {@link BloodmoonEvents#STARTED} is invoked.</p>
     *
     * @param durationTicks the duration of the bloodmoon in ticks
     * @throws IllegalArgumentException if {@code durationTicks} is not positive
     * @throws IllegalStateException if a bloodmoon is already active
     */
    public void startBloodmoon(int durationTicks) {
        if (durationTicks <= 0) throw new IllegalArgumentException("Duration must be positive.");
        if (this.isBloodmoonActive()) throw new IllegalStateException("Bloodmoon is already active.");

        this.updateAndSync(bloodmoon -> {
            bloodmoon.setTotalTicks(durationTicks);
            bloodmoon.setRemainingTicks(durationTicks);
            bloodmoon.setActive(true);
        });

        this.showBossBar();
        this.updateBossBar();

        BloodmoonEvents.STARTED.invoker().onStarted(this, durationTicks);
    }

    /**
     * Stops the active bloodmoon.
     *
     * <p>The stored duration is cleared, the state is synchronized, the boss bar is
     * hidden, and {@link BloodmoonEvents#STOPPED} is invoked.</p>
     *
     * @throws IllegalStateException if no bloodmoon is active
     */
    public void stopBloodmoon() {
        if (!this.isBloodmoonActive()) throw new IllegalStateException("Bloodmoon is not active.");

        this.updateAndSync(bloodmoon -> {
            bloodmoon.setTotalTicks(0);
            bloodmoon.setRemainingTicks(0);
            bloodmoon.setActive(false);
        });

        this.hideBossBar();

        BloodmoonEvents.STOPPED.invoker().onStopped(this);
    }

    /**
     * Sets the remaining duration of the active bloodmoon.
     *
     * <p>Providing a value less than or equal to zero stops the bloodmoon.</p>
     *
     * @param remainingTicks the new remaining duration in ticks
     * @throws IllegalStateException if no bloodmoon is active
     */
    public void setBloodmoonRemainingTicks(int remainingTicks) {
        if (!this.isBloodmoonActive()) throw new IllegalStateException("Bloodmoon is not active.");

        if (remainingTicks <= 0) {
            this.stopBloodmoon();
            return;
        }

        this.updateAndSync(bloodmoon -> bloodmoon.setRemainingTicks(remainingTicks));
        this.updateBossBar();
    }

    /**
     * Changes the remaining bloodmoon duration by the specified number of ticks.
     *
     * <p>A positive value extends the bloodmoon, while a negative value shortens it.
     * If the resulting duration is less than or equal to zero, the bloodmoon stops.</p>
     *
     * @param delta the number of ticks to add to the remaining duration
     * @throws ArithmeticException if the resulting duration overflows an {@code int}
     * @throws IllegalStateException if no bloodmoon is active
     */
    public void changeBloodmoonRemainingTicks(int delta) {
        this.setBloodmoonRemainingTicks(Math.addExact(this.getBloodmoonRemainingTicks(), delta));
    }

    /**
     * Returns the remaining bloodmoon duration.
     *
     * @return the remaining duration in ticks
     */
    public int getBloodmoonRemainingTicks() {
        return this.component.getRemainingTicks();
    }

    /**
     * Determines whether this bloodmoon is currently active.
     *
     * @return {@code true} if the bloodmoon is active; otherwise {@code false}
     */
    public boolean isBloodmoonActive() {
        return this.component.isActive();
    }

    /**
     * Registers the server tick and player connection event listeners used by the
     * bloodmoon manager.
     */
    private static void registerEvents() {
        if (eventsRegistered) return;

        ServerTickEvents.START_LEVEL_TICK.register(tickedLevel -> {
            Bloodmoon bloodmoon = instance;
            if (bloodmoon != null && bloodmoon.overworld == tickedLevel) bloodmoon.tick();
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            Bloodmoon bloodmoon = instance;
            if (bloodmoon != null && bloodmoon.isBloodmoonActive()) bloodmoon.bossBar.addPlayer(handler.player);
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Bloodmoon bloodmoon = instance;
            if (bloodmoon != null) bloodmoon.bossBar.removePlayer(handler.player);
        });

        eventsRegistered = true;
    }

    /**
     * Advances the active bloodmoon by one server tick.
     *
     * <p>The boss bar, component synchronization, and tick callback are updated once
     * per second rather than every tick.</p>
     */
    private void tick() {
        if (!this.isBloodmoonActive()) return;

        int remainingTicks = this.getBloodmoonRemainingTicks() - 1;
        this.component.setRemainingTicks(remainingTicks);

        if (remainingTicks <= 0) {
            this.stopBloodmoon();
            return;
        }

        if (remainingTicks % 20 != 0) return;

        this.updateBossBar();
        this.sync();

        BloodmoonEvents.TICK.invoker().onTick(this, this.getBloodmoonRemainingTicks());
    }

    /**
     * Applies the visual and audio settings used by the bloodmoon boss bar.
     */
    private void configureBossBar() {
        this.bossBar.setDarkenScreen(true);
        this.bossBar.setCreateWorldFog(false);
        this.bossBar.setPlayBossMusic(false);
        this.bossBar.setVisible(false);
    }

    /**
     * Adds all connected players to the boss bar and makes it visible.
     */
    private void showBossBar() {
        this.minecraftServer.getPlayerList().getPlayers().forEach(this.bossBar::addPlayer);
        this.bossBar.setVisible(true);
    }

    /**
     * Hides the boss bar and removes all tracked players from it.
     */
    private void hideBossBar() {
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    /**
     * Updates the boss bar progress and remaining-time text.
     */
    private void updateBossBar() {
        this.bossBar.setProgress(this.calculateProgress());
        this.bossBar.setName(getBossbarName(formatTime(this.getBloodmoonRemainingTicks())));
    }

    /**
     * Calculates the fraction of the original bloodmoon duration that remains.
     *
     * @return the remaining progress between {@code 0.0} and {@code 1.0}
     */
    private float calculateProgress() {
        int totalTicks = this.component.getTotalTicks();
        if (totalTicks <= 0) return 0.0F;

        return Mth.clamp((float) this.getBloodmoonRemainingTicks() / totalTicks, 0.0F, 1.0F);
    }

    /**
     * Restores the boss bar for a bloodmoon loaded from persistent state.
     */
    private void restoreBloodmoon() {
        this.showBossBar();
        this.updateBossBar();
    }

    /**
     * Applies a component update and synchronizes the resulting state.
     *
     * @param update the operation applied to the bloodmoon component
     */
    private void updateAndSync(Consumer<BloodmoonComponent> update) {
        update.accept(this.component);
        this.sync();
    }

    /**
     * Synchronizes the overworld bloodmoon component with connected clients.
     */
    private void sync() {
        BloodmoonComponent.KEY.sync(this.overworld);
    }

    /**
     * Formats a tick duration as minutes and seconds.
     *
     * @param ticks the duration in ticks
     * @return the duration formatted as {@code MM:SS}
     */
    private static String formatTime(int ticks) {
        int totalSeconds = Math.max(0, Math.ceilDiv(ticks, 20));
        return "%02d:%02d".formatted(totalSeconds / 60, totalSeconds % 60);
    }
}