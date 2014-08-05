package com.thisisnoble.javatest.events;

import com.thisisnoble.javatest.Event;

public class RiskEvent implements Event {

    private final String id;
    private final String parentId;
    private final double riskValue;

    public RiskEvent(String id, String parentId, double riskValue) {
        this.id = id;
        this.parentId = parentId;
        this.riskValue = riskValue;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public double getRiskValue() {
        return riskValue;
    }
}
