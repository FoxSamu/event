/*
 * Copyright 2021 Shadew
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shadew.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EventTests {
    @Test
    void testSimple() {
        EventType<Event> event = EventType.builder("test.simple", Event.class).build();
        Counter counter = new Counter(2);

        event.addCallback(evt -> counter.count());
        event.addCallback(evt -> counter.count());
        event.trigger(Event::new);

        counter.finish();
    }

    @Test
    void testMulti() {
        EventType<Event> one = EventType.builder("test.one", Event.class).build();
        EventType<Event> two = EventType.builder("test.two", Event.class).build();
        Counter oneCounter = new Counter(2);
        Counter twoCounter = new Counter(3);

        one.addCallback(evt -> oneCounter.count());
        one.addCallback(evt -> oneCounter.count());

        two.addCallback(evt -> twoCounter.count());
        two.addCallback(evt -> twoCounter.count());
        two.addCallback(evt -> twoCounter.count());

        one.trigger(Event::new);
        two.trigger(Event::new);

        oneCounter.finish();
        twoCounter.finish();
    }

    @Test
    void testCancel() {
        EventType<Event> event = EventType.builder("test.cancel.simple", Event.class).cancellable(true).build();

        event.addCallback(Event::cancel);
        assertTrue(event.trigger(Event::new).isCancelled());
    }

    @Test
    void testCancelPropagate() {
        EventType<Event> event = EventType.builder("test.cancel.propagate", Event.class).cancellable(true).build();
        Counter counter = new Counter(2);

        event.addCallback(evt -> {
            evt.cancel();
            counter.count();
        });
        event.addCallback(evt -> {
            assertTrue(evt.isCancelled());
            counter.count();
        });

        assertTrue(event.trigger(Event::new).isCancelled());
        counter.finish();
    }

    @Test
    void testCancelNopropagate() {
        EventType<Event> event = EventType.builder("test.cancel.nopropagate", Event.class).cancellable(true).propagateWhenCancelled(false).build();
        Counter counter = new Counter(1);

        event.addCallback(evt -> {
            evt.cancel();
            counter.count();
        });
        event.addCallback(evt -> {
            assertTrue(evt.isCancelled());
            counter.count();
        });

        assertTrue(event.trigger(Event::new).isCancelled());
        counter.finish();
    }

    @Test
    void testCustomType() {
        EventType<IntEvent> event = EventType.builder("test.customtype", IntEvent.class).build();
        Counter counter = new Counter(6);

        event.addCallback(evt -> {
            assertEquals(3, evt.getValue());
            counter.count(evt.getValue());
        });
        event.addCallback(evt -> {
            assertEquals(3, evt.getValue());
            counter.count(evt.getValue());
        });
        event.trigger(type -> new IntEvent(type, 3));

        counter.finish();
    }

    @Test
    void testEventCreationTypeCheck() {
        EventType<IntEvent> event = EventType.builder("test.typecheck.creation", IntEvent.class).build();
        assertThrows(IllegalArgumentException.class, () -> new Event(event));
        assertThrows(IllegalArgumentException.class, () -> new StringEvent(event, "hello"));
        new IntEvent(event, 3); // This must work
    }

    @Test
    void testEventTriggerTypeCheck() {
        EventType<Event> a = EventType.builder("test.typecheck.trigger.a", Event.class).build();
        EventType<Event> b = EventType.builder("test.typecheck.trigger.b", Event.class).build();
        assertThrows(IllegalArgumentException.class, () -> a.trigger(new Event(b)));
        assertThrows(IllegalArgumentException.class, () -> b.trigger(new Event(a)));
        a.trigger(new Event(a)); // This must work
        b.trigger(new Event(b)); // This must also work
    }

    @Test
    void testThrowExceptionHandler() {
        EventType<Event> event = EventType.builder("test.exception.throw", Event.class).exceptionHandler(EventType.THROW_EVENT_EXCEPTIONS).build();
        event.addCallback(evt -> {
            throw new Exception("owo I'm an exception");
        });

        EventException exc = assertThrows(EventException.class, () -> event.trigger(Event::new));
        assertNotNull(exc.getCause());
        assertEquals("owo I'm an exception", exc.getCause().getMessage());
    }

    @Test
    void testSuppressExceptionHandler() {
        EventType<Event> event = EventType.builder("test.exception.suppress", Event.class).exceptionHandler(EventType.SUPPRESS_EVENT_EXCEPTIONS).build();
        event.addCallback(evt -> {
            throw new Exception("owo I'm an exception");
        });

        event.trigger(Event::new); // This must work
    }

    private static class IntEvent extends Event {
        private final int val;

        IntEvent(EventType<?> type, int val) {
            super(type);
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }

    private static class StringEvent extends Event {
        private final String val;

        StringEvent(EventType<?> type, String val) {
            super(type);
            this.val = val;
        }

        public String getValue() {
            return val;
        }
    }

    private static class Counter {
        private final int expected;
        private int counted = 0;

        private Counter(int expected) {
            this.expected = expected;
        }

        public void count() {
            counted++;
        }

        public void count(int n) {
            counted += n;
        }

        public void finish() {
            assertEquals(expected, counted, "Incorrect invocation count");
        }
    }
}
