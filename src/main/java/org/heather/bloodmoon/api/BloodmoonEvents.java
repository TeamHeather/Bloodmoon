package org.heather.bloodmoon.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Provides lifecycle events for observing bloodmoon state changes.
 *
 * <p>Listeners can subscribe to notifications for when a bloodmoon starts,
 * advances, or stops. Events are invoked by {@link Bloodmoon} on the server
 * thread.</p>
 */
public final class BloodmoonEvents {

    /**
     * Invoked after a bloodmoon starts.
     *
     * <p>The callback receives the active bloodmoon manager and its initial
     * duration in ticks.</p>
     */
    public static final Event<Started> STARTED = EventFactory.createArrayBacked(Started.class, listeners -> (bloodmoon, durationTicks) -> {
        for (Started listener : listeners) listener.onStarted(bloodmoon, durationTicks);
    });

    /**
     * Invoked while a bloodmoon is active when its synchronized timer is updated.
     *
     * <p>The callback receives the bloodmoon manager and its current remaining
     * duration in ticks.</p>
     */
    public static final Event<Tick> TICK = EventFactory.createArrayBacked(Tick.class, listeners -> (bloodmoon, remainingTicks) -> {
        for (Tick listener : listeners) listener.onTick(bloodmoon, remainingTicks);
    });

    /**
     * Invoked after an active bloodmoon stops.
     */
    public static final Event<Stopped> STOPPED = EventFactory.createArrayBacked(Stopped.class, listeners -> bloodmoon -> {
        for (Stopped listener : listeners) listener.onStopped(bloodmoon);
    });

    private BloodmoonEvents() {}

    /**
     * Handles the start of a bloodmoon.
     */
    @FunctionalInterface
    public interface Started {

        /**
         * Called after a bloodmoon starts.
         *
         * @param bloodmoon the active bloodmoon manager
         * @param durationTicks the initial bloodmoon duration in ticks
         */
        void onStarted(Bloodmoon bloodmoon, int durationTicks);
    }

    /**
     * Handles periodic updates during an active bloodmoon.
     */
    @FunctionalInterface
    public interface Tick {

        /**
         * Called when the bloodmoon timer is updated.
         *
         * @param bloodmoon the active bloodmoon manager
         * @param remainingTicks the remaining bloodmoon duration in ticks
         */
        void onTick(Bloodmoon bloodmoon, int remainingTicks);
    }

    /**
     * Handles the end of a bloodmoon.
     */
    @FunctionalInterface
    public interface Stopped {

        /**
         * Called after a bloodmoon stops.
         *
         * @param bloodmoon the bloodmoon manager that stopped
         */
        void onStopped(Bloodmoon bloodmoon);
    }
}