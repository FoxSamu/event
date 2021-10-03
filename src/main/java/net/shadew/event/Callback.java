package net.shadew.event;

/**
 * A callback for an event.
 */
public interface Callback<E extends Event> {
    /**
     * Called when the event for this callback occurs. A callback may throw any exception, which is then handled by the
     * event type itself.
     *
     * @param event The event instance.
     */
    void handle(E event) throws Throwable;
}
