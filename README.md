# Event
A tiny event creation and handling library for Java.

## Installation
The library is available on my maven: https://maven.shadew.net/
- Group: `net.shadew`
- Artifact: `event`
- Version: `1.0`

To add via Gradle:
```gradle
repositories {
    maven { url 'https://maven.shadew.net/' }
}

dependencies {
    implementation 'net.shadew:event:1.0'
}
```

## Usage
The API is simple and easy to use. To trigger events, you must first obtain an `EventType` instance. `EventType`s are built throught a builder and are usually assigned to `public static final` fields.
```java
public static final EventType<Event> MY_EVENT 
    = EventType.builder("my.event", Event.class).build();
```

This simple event type accepts a raw `Event` instance and can be used to trigger the event or add callbacks to. Note that it has a name: this name can be used to check the event quickly so it must be a recognizable name. While it can be any name, it is recommended to stay with `lower_snake_case` separated by periods to group events.

To add a callback, you simply call `MY_EVENT.addCallback`:
```java
MY_EVENT.addCallback(event -> {
    System.out.println("Event occurred!");
});
```

If you add multiple callbacks, they are invoked in the order that they are added:
```java
MY_EVENT.addCallback(event -> {
    System.out.println("I invoke first!");
});
MY_EVENT.addCallback(event -> {
    System.out.println("I invoke second!");
});
MY_EVENT.addCallback(event -> {
    System.out.println("I invoke last!");
});
```

To trigger the event, you call `MY_EVENT.trigger`:
```java
MY_EVENT.trigger(new Event(MY_EVENT));

// You can create an event and pass it directly, but it is
// usually more convenient to use a factory, as exceptions
// will arise when an event is created with the wrong type
// (this is more copy-paste-proof)
MY_EVENT.trigger(type -> new Event(type));
// Or just use a method reference
MY_EVENT.trigger(Event::new);
```

### Overriding `Event`
You can simply override the `Event` class if you need to provide more context to an event. You can also override if you want to allow callbacks to modify what happens after the event.

```java
public class MyEvent extends Event {
    private final String message;

    public MyEvent(EventType<?> type, String message) {
        super(type);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
```

Then create your event type with your `MyEvent` class:

```java
public static final EventType<MyEvent> MY_EVENT 
    = EventType.builder("my.event", MyEvent.class).build();
```

Now when you register callbacks, the callbacks get passed a `MyEvent` instance instead, and you can directly look up the message from it. When triggering the event, you now also have to provide a `MyEvent` instance.

```java
MY_EVENT.addCallback(event -> {
    System.out.println(event.getMessage());
});

MY_EVENT.trigger(type -> new MyEvent(type, "Hello world!"));
// The callback will now print 'Hello world!'
```

### Cancelling events
You may want to create events that can be cancelled by callbacks. While it is possible to add a `cancellable` field to your custom event class, this library provides an easy alternative.

Upon building an `EventType` you can specify whether the event must be cancellable or not:

```java
public static final EventType<Event> MY_EVENT
    = EventType.builder("my.event", Event.class)
               .cancellable(true)
               .build();
```

Then in your callbacks, you can cancel the event using `event.cancel()`. Note that this method exists even for not-cancellable events: when it is called while the method is not cancellable, an exception is thrown.

```java
MY_EVENT.addCallback(event -> {
    System.out.println("Cancelling...");
    event.cancel();
});

if (MY_EVENT.trigger(Event::new).isCancelled()) {
    System.out.println("Event was cancelled");
}
```

### Propagating events
Callbacks of events are invoked in the order they are added. Usually it's not necessary to stop subsequent callbacks from executing but it might be useful in certain cases. Like cancelling, this can be configured directly in the `EventType`, however, you can also override `mustPropagate` in a custom event class for an advanced strategy.

```java
public static final EventType<Event> MY_EVENT
    = EventType.builder("my.event", Event.class)
               .canStopPropagation(true)
               .build();
```

Stopping the propagation avoids other callbacks that have not been called from being called.

```java
MY_EVENT.addCallback(event -> {
    System.out.println("Stopping propagation...");
    event.stopPropagation();
});
MY_EVENT.addCallback(event -> {
    System.out.println("I won't execute :(");
});

MY_EVENT.trigger(Event::new);
```

Often, stopping propagation can be combined with cancelling events to pretend the event didn't happen for subsequent callbacks after the event has been cancelled.

### Exceptions
A callback may throw any exception, even checked exceptions. By default, when an exception occurs, the exception is wrapped in an unchecked `EventException`, but one may provide a custom exception handling strategy per `EventType`, by providing it in the builder.

```java
public static final EventType<Event> MY_EVENT
    = EventType.builder("my.event", Event.class)
               .exceptionHandler(EventType.SUPPRESS_EVENT_EXCEPTIONS)
               .build();
```

```java
MY_EVENT.addCallback(event -> {
    throw new Exception("I am an exception");
});
MY_EVENT.addCallback(event -> {
    throw new Exception("I am another exception");
});

MY_EVENT.trigger(Event::new);
// Exceptions are suppressed
```

You can either use one of the predefined `ExceptionHandler`s or implement your own. The predefined exception handlers are  constants of `EventType`.
- `THROW_EVENT_EXCEPTIONS`: rethrows the exception
- `PRINT_EVENT_EXCEPTIONS`: prints the exception stack trace to the `System.err`
- `SUPPRESS_EVENT_EXCEPTIONS`: hides the exception entirely

Note that the `trigger` method always throws an `EventException` if the exception handler rethrows an exception. If the exception handler rethrows an exception, the first time it is converted to an `EventException` (either by wrapping or casting if possible). Subsequent exceptions are added as suppressed exceptions. Exceptions never stop the propagation to subsequent callbacks.

```java
public static final EventType<Event> MY_EVENT
    = EventType.builder("my.event", Event.class)
               .exceptionHandler(EventType.THROW_EVENT_EXCEPTIONS)
               .build();
```

```java
MY_EVENT.addCallback(event -> {
    System.out.println("I execute :)");
    throw new Exception("I am an exception");
});
MY_EVENT.addCallback(event -> {
    System.out.println("I also execute :)");
    throw new Exception("I am another exception");
});

MY_EVENT.trigger(Event::new);
// EventException is thrown
```

## License

Copyright 2021 Shadew

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
