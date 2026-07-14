package io.github.pepe3012.bloodmoon.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class BloodmoonEvents {
    public static final Event<Started> STARTED = EventFactory.createArrayBacked(Started.class, listeners -> (bloodmoon, durationTicks) -> {
        for (Started listener : listeners) listener.onStarted(bloodmoon, durationTicks);
    });

    public static final Event<Tick> TICK = EventFactory.createArrayBacked(Tick.class, listeners -> (bloodmoon, remainingTicks) -> {
        for (Tick listener : listeners) listener.onTick(bloodmoon, remainingTicks);
    });

    public static final Event<Stopped> STOPPED = EventFactory.createArrayBacked(Stopped.class, listeners -> bloodmoon -> {
        for (Stopped listener : listeners) listener.onStopped(bloodmoon);
    });

    private BloodmoonEvents() {}

    @FunctionalInterface
    public interface Started {
        void onStarted(Bloodmoon bloodmoon, int durationTicks);
    }

    @FunctionalInterface
    public interface Tick {
        void onTick(Bloodmoon bloodmoon, int remainingTicks);
    }

    @FunctionalInterface
    public interface Stopped {
        void onStopped(Bloodmoon bloodmoon);
    }
}