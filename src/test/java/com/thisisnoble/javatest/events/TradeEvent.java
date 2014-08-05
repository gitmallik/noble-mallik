package com.thisisnoble.javatest.events;

import com.thisisnoble.javatest.Event;

public class TradeEvent implements Event {

    private final String id;
    private final double notional;

    public TradeEvent(String id, double notional) {
        this.id = id;
        this.notional = notional;
    }

    public String getId() {
        return id;
    }

    public double getNotional() {
        return notional;
    }
}
