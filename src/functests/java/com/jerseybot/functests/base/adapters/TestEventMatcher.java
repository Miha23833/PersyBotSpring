package com.jerseybot.functests.base.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.awaitility.Awaitility;

public class TestEventMatcher extends ListenerAdapter {
    private final JDA jda;

    private final List<Predicate<GenericEvent>> awaitingEvents = new ArrayList<>();

    public TestEventMatcher(JDA jda) {
        this.jda = jda;
    }

    public <T extends GenericEvent> TestEventMatcher addEvent(Class<T> target, Predicate<T> predicate) {
        awaitingEvents.add(e -> target.isInstance(e) && predicate.test(target.cast(e)));
        return this;
    }

    public void await() {
        await(3, TimeUnit.SECONDS);
    }

    public void await(int timeout, TimeUnit timeUnit) {
        jda.addEventListener(this);
        Awaitility.await().atMost(timeout, timeUnit).until(this.awaitingEvents::isEmpty);
        jda.removeEventListener(this);
    }

    @Override
    public void onGenericEvent(@NotNull GenericEvent event) {
        awaitingEvents.removeIf(predicate -> predicate.test(event));
    }
}
