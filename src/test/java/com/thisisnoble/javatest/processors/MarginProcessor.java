package com.thisisnoble.javatest.processors;

import com.thisisnoble.javatest.Event;
import com.thisisnoble.javatest.Orchestrator;
import com.thisisnoble.javatest.events.MarginEvent;
import com.thisisnoble.javatest.events.ShippingEvent;
import com.thisisnoble.javatest.events.TradeEvent;

import static com.thisisnoble.javatest.util.TestIdGenerator.marginEventId;

public class MarginProcessor extends AbstractProcessor {

    public MarginProcessor(Orchestrator orchestrator) {
        super(orchestrator);
    }

    @Override
    public boolean interestedIn(Event event) {
        return event instanceof TradeEvent || event instanceof ShippingEvent;
    }

    @Override
    protected Event processInternal(Event event) {
        String parId = event.getId();
        if (event instanceof TradeEvent)
            return new MarginEvent(marginEventId(parId), parId, calculateTradeMargin(event));
        else if (event instanceof ShippingEvent)
            return new MarginEvent(marginEventId(parId), parId, calculateShippingMargin(event));
        throw new IllegalArgumentException("unknown event for margin calculation " + event);
    }

    private double calculateShippingMargin(Event se) {
        return ((ShippingEvent) se).getShippingCost() * 0.01;
    }

    private double calculateTradeMargin(Event te) {
        return ((TradeEvent) te).getNotional() * 0.01;
    }
}
