package net.shadew.event;

/**
 * An instance of an event, to be provided to and, if possible, modified by {@link Callback}s. This class may be
 * overridden to add extra context to the event.
 */
public class Event {
    private final EventType<?> type;
    private boolean cancelled = false;

    /**
     * Constructs an event instance for the given type.
     *
     * @param type The event type, which must accept the class of this event instance.
     * @throws IllegalArgumentException If the given event type does not accept this event instance
     */
    public Event(EventType<?> type) {
        this.type = type;
        if (!type.getEventClass().isAssignableFrom(getClass())) {
            throw new IllegalArgumentException("Event type does not support the event class " + getClass().getName());
        }
    }

    /**
     * Returns true when the name of this event's type matches the given name.
     */
    public final boolean isType(String type) {
        return this.type.getName().equals(type);
    }

    /**
     * Returns true when this event's type matches the given type.
     */
    public final boolean isType(EventType<?> type) {
        return this.type == type;
    }

    /**
     * Calls the given {@link Callback} if this event's type is the given type.
     *
     * @param type     The type to match.
     * @param callback The callback to call.
     */
    public final <E extends Event> void ifType(EventType<? extends E> type, Callback<? super E> callback) throws Throwable {
        if (isType(type))
            callback.handle(type.getEventClass().cast(this));
    }

    /**
     * Runs the given {@link Runnable} if this event's type is the given type.
     *
     * @param type     The type to match.
     * @param callback The runnable to run.
     */
    public final <E extends Event> void ifType(EventType<? extends E> type, Runnable callback) {
        if (isType(type))
            callback.run();
    }

    /**
     * Calls the given {@link Callback} if this event's type has the given name.
     *
     * @param type     The type to match.
     * @param callback The callback to call.
     */
    public final void ifType(String type, Callback<Event> callback) throws Throwable {
        if (isType(type))
            callback.handle(this);
    }

    /**
     * Runs the given {@link Runnable} if this event's type has the given name.
     *
     * @param type     The type to match.
     * @param callback The runnable to run.
     */
    public final void ifType(String type, Runnable callback) {
        if (isType(type))
            callback.run();
    }

    /**
     * Returns the {@link EventType} of this event.
     */
    public final EventType<?> getType() {
        return type;
    }

    /**
     * Returns the name of this event.
     */
    public final String getName() {
        return type.getName();
    }

    /**
     * Sets the cancelled status of this event. This event must be cancellable.
     *
     * @param cancelled The cancelled status.
     * @throws IllegalStateException If this event is not cancellable. Trown even if the cancelled status is attempted
     *                               to be set to false while the event is not cancellable.
     */
    public final void setCancelled(boolean cancelled) {
        if (!type.isCancellable())
            throw new IllegalStateException("Event '" + type.getName() + "' is not cancellable");

        this.cancelled = cancelled;
    }

    /**
     * Returns whether this event is cancellable.
     */
    public final boolean isCancellable() {
        return type.isCancellable();
    }

    /**
     * Returns whether this event is cancelled. When the event is not cancellable, this method returns false by
     * definition.
     */
    public final boolean isCancelled() {
        return cancelled;
    }

    /**
     * Cancels this event, i.e. sets the cancelled status of this event to true. This event must be cancellable.
     *
     * @throws IllegalStateException If this event is not cancellable.
     */
    public final void cancel() {
        setCancelled(true);
    }

    /**
     * Returns whether subsequent callbacks must still be invoked. By default, this returns always false unless the
     * event is cancelled and the event type disabled propagation for cancelled events. This method may be overridden to
     * provide a custom propagation strategy.
     */
    public boolean mustPropagate() {
        if (type.doesPropagateWhenCancelled())
            return true;
        return !cancelled;
    }
}
