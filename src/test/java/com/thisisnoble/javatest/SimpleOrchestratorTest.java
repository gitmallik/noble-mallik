package com.thisisnoble.javatest;

import com.thisisnoble.javatest.events.MarginEvent;
import com.thisisnoble.javatest.events.RiskEvent;
import com.thisisnoble.javatest.events.ShippingEvent;
import com.thisisnoble.javatest.events.TradeEvent;
import com.thisisnoble.javatest.impl.CompositeEvent;
import com.thisisnoble.javatest.processors.MarginProcessor;
import com.thisisnoble.javatest.processors.RiskProcessor;
import com.thisisnoble.javatest.processors.ShippingProcessor;
import org.junit.Test;

import static com.thisisnoble.javatest.util.TestIdGenerator.tradeEventId;
import static org.junit.Assert.*;

public class SimpleOrchestratorTest {

    @Test
    public void tradeEventShouldTriggerAllProcessors() {
        TestPublisher testPublisher = new TestPublisher();
        Orchestrator orchestrator = setupOrchestrator();
        orchestrator.setup(testPublisher);

        TradeEvent te = new TradeEvent(tradeEventId(), 1000.0);
        orchestrator.receive(te);
        safeSleep(3000);
        CompositeEvent ce = (CompositeEvent) testPublisher.getLastEvent();
        assertEquals(te, ce.getParent());
        assertEquals(5, ce.size());
        RiskEvent re1 = ce.getChildById("tradeEvt-riskEvt");
        assertNotNull(re1);
        assertEquals(50.0, re1.getRiskValue(), 0.01);
        MarginEvent me1 = ce.getChildById("tradeEvt-marginEvt");
        assertNotNull(me1);
        assertEquals(10.0, me1.getMargin(), 0.01);
        ShippingEvent se1 = ce.getChildById("tradeEvt-shipEvt");
        assertNotNull(se1);
        assertEquals(200.0, se1.getShippingCost(), 0.01);
        RiskEvent re2 = ce.getChildById("tradeEvt-shipEvt-riskEvt");
        assertNotNull(re2);
        assertEquals(10.0, re2.getRiskValue(), 0.01);
        MarginEvent me2 = ce.getChildById("tradeEvt-shipEvt-marginEvt");
        assertNotNull(me2);
        assertEquals(2.0, me2.getMargin(), 0.01);
    }

    @Test
    public void shippingEventShouldTriggerOnly2Processors() {
        TestPublisher testPublisher = new TestPublisher();
        Orchestrator orchestrator = setupOrchestrator();
        orchestrator.setup(testPublisher);

        ShippingEvent se = new ShippingEvent("ship2", 500.0);
        orchestrator.receive(se);
        safeSleep(3000);
        CompositeEvent ce = (CompositeEvent) testPublisher.getLastEvent();
        //assertEquals(se, ce.getParent());
        assertEquals(2, ce.size());
        RiskEvent re2 = ce.getChildById("ship2-riskEvt");
        assertNotNull(re2);
        assertEquals(25.0, re2.getRiskValue(), 0.01);
        MarginEvent me2 = ce.getChildById("ship2-marginEvt");
        assertNotNull(me2);
        assertEquals(5.0, me2.getMargin(), 0.01);
    }
    
    @Test
    public void testCode() 
    {
        TestPublisher testPublisher = new TestPublisher();
        Orchestrator orchestrator = setupOrchestrator();
        orchestrator.setup(testPublisher);
        
        for (int i=0;i<100;i++)
        {
        	TradeEvent te = new TradeEvent(tradeEventId()+i, 1000.0);
        	orchestrator.receive(te);
        	safeSleep(3000);
        	CompositeEvent ce = (CompositeEvent) testPublisher.getLastEvent();
        	assertEquals(te, ce.getParent());
        	assertEquals(5, ce.size());
        }
    }
    
    private Orchestrator setupOrchestrator() {
        Orchestrator orchestrator = createOrchestrator();
        orchestrator.register(new RiskProcessor(orchestrator));
        orchestrator.register(new MarginProcessor(orchestrator));
        orchestrator.register(new ShippingProcessor(orchestrator));
        return orchestrator;
    }

    private void safeSleep(long l) {
        try {
            Thread.sleep(l);
        } catch (InterruptedException e) {
            //ignore
        }
    }

    private Orchestrator createOrchestrator() 
    {
        return new MyOrchestrator();
    }
}
