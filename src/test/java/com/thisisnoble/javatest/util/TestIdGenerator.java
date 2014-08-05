package com.thisisnoble.javatest.util;

public class TestIdGenerator {

    public static String tradeEventId() {
        return "tradeEvt";
    }

    public static String shipEventId(String parId) {
        return parId == null ? "shipEvt" : parId + "-shipEvt";
    }

    public static String riskEventId(String parId) {
        return parId + "-riskEvt";
    }

    public static String marginEventId(String parId) {
        return parId + "-marginEvt";
    }
}
