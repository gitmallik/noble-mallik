/**
 * @author Mallik Kandula
 *
 * Implementation of Orchestrator interface.
 *
 */
package com.thisisnoble.javatest;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.logging.Level;
//import java.util.logging.Logger;

import com.thisisnoble.javatest.impl.CompositeEvent;

public class MyOrchestrator implements Orchestrator 
{
	private List<Processor> processors = Collections.synchronizedList(new ArrayList<Processor>());
	private ConcurrentMap<String, CompositeEvent> ceMap = new ConcurrentHashMap<String, CompositeEvent>();
	private Publisher publisher = null;
	//Lock myLock = new ReentrantLock();
	
	/*	using a plain LinkedBlockingQueue as queue size is dynamic, the performance is good enough 
	 	when you are delivering some micro-second level systems. We can visit ArrayBlockingQueue once we can estimate 
		the queue size needed by [Queue Size] = [Max acceptable delay] * [Max message rate].
		making sure the consumers catch up without the queue size growing tremendously.
	*/
	private final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
	private final ExecutorService threadPool =  Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public MyOrchestrator()
	{
		//initialize logging, etc.
		ProcessQueueThread pt = new ProcessQueueThread(eventQueue);
		pt.start();
	}
	
	/* (non-Javadoc)
	 * @see com.thisisnoble.javatest.Orchestrator#register(com.thisisnoble.javatest.Processor)
	 */
	@Override
	public void register(Processor processor) 
	{
		if (processor != null)
			processors.add(processor);
		
		System.out.println("Registered processor:"+ processor.toString());
	}
	
	/* (non-Javadoc)
	 * @see com.thisisnoble.javatest.Orchestrator#setup(com.thisisnoble.javatest.Publisher)
	 */
	@Override
	public void setup(Publisher publisher)
	{
		this.publisher = publisher;
	}

	/* (non-Javadoc)
	 * @see com.thisisnoble.javatest.Orchestrator#receive(com.thisisnoble.javatest.Event)
	 */
	@Override
	public void receive(Event event) 
	{
		//we dont need to synchronize the queue as BlockingQueue implementations are thread-safe. but any data corruptions issues in the future addition of code.
		synchronized(eventQueue)
		{
			if (event != null)
			{
				System.out.println("Received event:"+ event.getId()+", class: "+ event.getClass().getName());
				try
				{
					//keep the events moving by pushing into a queue.
					eventQueue.put(event);
					System.out.println("enqueud event:"+ event.getId()+", class: "+ event.getClass().getName());
					
					String key = event.getId();
					if (key.indexOf("-") > -1)
						key = key.substring(0, key.indexOf("-"));
					
					System.out.println("composite key: "+ key);
					if (ceMap.containsKey(key))
					{
						System.out.println("Already contains key: "+ key+", for event: "+ event.getId());
						CompositeEvent ceEvt = ceMap.get(key);
						ceEvt.addChild(event);
					}
					else
					{
						ceMap.put(key, new CompositeEvent(key, event));
					}
				}
				catch(InterruptedException ie_)
		    	{
		    		System.out.println("error in receive method: "+ ie_.getMessage());
		    	}
				finally
				{
				}
			}
		}
	}
	
	private class ProcessQueueThread extends Thread 
	{
        private Event input;
        private BlockingQueue<Event> eventQueue;
        
        public ProcessQueueThread(BlockingQueue<Event> eventQueue) 
        {
        	this.eventQueue = eventQueue;
        }

        @Override
        public void run() 
        {
        	try
    		{
    			System.out.println("Starting ProcessQueueThread ..");
        		while (true)
    			{
        			//we have atleast 1 event to process
    				if (eventQueue != null && !eventQueue.isEmpty())
    				{
		        		input = eventQueue.take();
		        		System.out.println("processThread received event:"+ input.getId()+", class: "+ 
		        								input.getClass().getName());
		        		if (input !=null)
		        			threadPool.submit(new RunnerTask(input));
	    			}
	    			else
	    			{
	    				System.out.println("Queue is empty or not initialized, sleeping for a sec ..");
	    				
	    				//sleep 1 second.
	    				Thread.sleep(1000);
	    			}
    			}
    		}
        	catch(InterruptedException ie_)
        	{
        		//Logger.getLogger(MyOrchestrator.class.getName()).log(Level.SEVERE, 
        				//"Process thread interrupted, exiting ..", ie_);
        		
        		System.out.println("Process thread interrupted, exiting .."+ ie_.getMessage());
        	}
    		finally
    		{
    		}
        }
    }
	
	private class RunnerTask implements Runnable 
	{
        private final Event evt;

        private RunnerTask(Event evt) 
        {
            this.evt = evt;
        }

        @Override
        public void run() 
        {
        	try
        	{
	        	if (processors.size() > 0)
				{
					for (Processor processor: processors)
					{
						//picking the right processor below. Scaling & throughput 
						//of processing should be designed in the processor impl
						//based on how many events are more inclined towards a particular event
						if (processor.interestedIn(evt))
						{
							System.out.println("In runner, processor class "+ processor.getClass().getName()+", is processing event:"+
									 				evt.getId()+", class: "+ evt.getClass().getName());
							processor.process(evt);
						}
					}
					
					//Thread.sleep(3000); //Need to implement a barrier here maybe so results can be collected for all processor results, dont know requirements.
					
					//publish compositeMap to bus
					String key = evt.getId();
					if (key.indexOf("-") > -1)
						key = key.substring(0, key.indexOf("-"));
					
					if (ceMap != null && ceMap.containsKey(key))
					{
						publisher.publish(ceMap.get(key));
						System.out.println("published composite event for key: "+ key);
					}
				}
				else
				{
					//throw away the event as no processors registered yet
				}
        	}
        	catch(Exception e_)
        	{
        		System.out.println("Runner thread exception .."+ e_.getMessage());
        	}
    		finally
    		{
    		}
            
        }
    }

}
