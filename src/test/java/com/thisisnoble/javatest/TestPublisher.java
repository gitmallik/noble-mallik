package com.thisisnoble.javatest;

public class TestPublisher implements Publisher {

    private Event lastEvent;

    @Override
    public void publish(Event event) {
        this.lastEvent = event;
    }

    public Event getLastEvent() {
        Event result = lastEvent;
        lastEvent = null;
        return result;
    }
}
