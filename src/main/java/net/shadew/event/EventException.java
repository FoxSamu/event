package net.shadew.event;

/**
 * An exception that can be thrown in {@link EventType.ExceptionHandler}s to wrap a thrown exception. This exception
 * must have a not-null cause.
 */
public class EventException extends RuntimeException {
    public EventException(String message, Throwable cause) {
        super(message, checkNotNull(cause));
    }

    public EventException(Throwable cause) {
        super(checkNotNull(cause));
    }

    private static Throwable checkNotNull(Throwable cause) {
        if (cause == null)
            throw new NullPointerException("Null cause");
        return cause;
    }
}
