package io.github.pepe3012.bloodmoon.api;

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

public final class Bloodmoon {

    public static final Vector3fc MOON_COLOR = new Vector3f(0.65F, 0.02F, 0.02F);
    public static final String BLOODMOON_REMAINING = "bossbar.bloodmoon.remaining";

    private static Bloodmoon instance;
    private static boolean eventsRegistered;

    private final MinecraftServer minecraftServer;
    private final ServerLevel overworld;
    private final BloodmoonComponent component;
    private final ServerBossEvent bossBar;

    private Bloodmoon(MinecraftServer minecraftServer) {
        this.minecraftServer = minecraftServer;
        this.overworld = minecraftServer.overworld();
        this.component = BloodmoonComponent.KEY.get(this.overworld);
        this.bossBar = new ServerBossEvent(UUID.randomUUID(), Component.empty(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

        this.configureBossBar();

        if (this.isBloodmoonActive()) this.restoreBloodmoon();
    }

    public static void initialize(MinecraftServer minecraftServer) {
        instance = new Bloodmoon(Objects.requireNonNull(minecraftServer, "minecraftServer"));
        registerEvents();
    }

    public static Component getBossBarName(String remainingTime) {
        return Component.translatable(BLOODMOON_REMAINING, remainingTime);
    }

    public static Bloodmoon getInstance() {
        if (instance == null) throw new IllegalStateException("Bloodmoon has not been initialized.");
        return instance;
    }

    public static boolean isBloodmoonActive(Level level) {
        Objects.requireNonNull(level, "level");
        return BloodmoonComponent.KEY.get(level).isActive();
    }

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

    public void setBloodmoonRemainingTicks(int remainingTicks) {
        if (!this.isBloodmoonActive()) throw new IllegalStateException("Bloodmoon is not active.");

        if (remainingTicks <= 0) {
            this.stopBloodmoon();
            return;
        }

        this.updateAndSync(bloodmoon -> bloodmoon.setRemainingTicks(remainingTicks));
        this.updateBossBar();
    }

    public void changeBloodmoonRemainingTicks(int delta) {
        this.setBloodmoonRemainingTicks(Math.addExact(this.getBloodmoonRemainingTicks(), delta));
    }

    public int getBloodmoonRemainingTicks() {
        return this.component.getRemainingTicks();
    }

    public boolean isBloodmoonActive() {
        return this.component.isActive();
    }

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

    private void configureBossBar() {
        this.bossBar.setDarkenScreen(true);
        this.bossBar.setCreateWorldFog(false);
        this.bossBar.setPlayBossMusic(false);
        this.bossBar.setVisible(false);
    }

    private void showBossBar() {
        this.minecraftServer.getPlayerList().getPlayers().forEach(this.bossBar::addPlayer);
        this.bossBar.setVisible(true);
    }

    private void hideBossBar() {
        this.bossBar.setVisible(false);
        this.bossBar.removeAllPlayers();
    }

    private void updateBossBar() {
        this.bossBar.setProgress(this.calculateProgress());
        this.bossBar.setName(Component.translatable(BLOODMOON_REMAINING, formatTime(this.getBloodmoonRemainingTicks())));
    }

    private float calculateProgress() {
        int totalTicks = this.component.getTotalTicks();
        if (totalTicks <= 0) return 0.0F;

        return Mth.clamp((float) this.getBloodmoonRemainingTicks() / totalTicks, 0.0F, 1.0F);
    }

    private void restoreBloodmoon() {
        this.showBossBar();
        this.updateBossBar();
    }

    private void updateAndSync(Consumer<BloodmoonComponent> update) {
        update.accept(this.component);
        this.sync();
    }

    private void sync() {
        BloodmoonComponent.KEY.sync(this.overworld);
    }

    private static String formatTime(int ticks) {
        int totalSeconds = Math.max(0, Math.ceilDiv(ticks, 20));
        return "%02d:%02d".formatted(totalSeconds / 60, totalSeconds % 60);
    }
}