package com.haines.ml.rce.model;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class PipelinedEventConsumerUnitTest {
	
	private final static Logger LOG = LoggerFactory.getLogger(PipelinedEventConsumerUnitTest.class);

	private PipelinedEventConsumer<TestEvent, TestEventConsumer> candidate;
	private TestEventConsumer live = new TestEventConsumer("live");
	private TestEventConsumer staging = new TestEventConsumer("staging");
	
	@Before
	public void before(){
		this.candidate = new PipelinedEventConsumer<TestEvent, TestEventConsumer>(live, staging);
	}
	
	@Test
	public void givenCandidateAnd2Threads_whenCallingSwitchConsumer_thenOwnershipOfConsumerTransferedBetweenThreads() throws InterruptedException{
		final CountDownLatch starter = new CountDownLatch(2);
		final AtomicBoolean stopped = new AtomicBoolean(false);
		final AtomicBoolean inError = new AtomicBoolean(false);
		final Thread thread1 = new Thread(new Runnable(){

			@Override
			public void run() {
				starter.countDown();
				while(!stopped.get()){
					candidate.consume(new TestEvent(1));
				}
			}
			
		});
		
		Thread thread2 = new Thread(new Runnable(){

			@Override
			public void run() {
				starter.countDown();
				
				TestEventConsumer currentLive = PipelinedEventConsumerUnitTest.this.live;
				try {
					while(!stopped.get()){
						Thread.sleep(100);
						candidate.switchLiveConsumer(); // we now own live
						LOG.debug("Switching consumers. "+currentLive.name+" is now offline");
						
						assertThat(currentLive.name+" consumer has active execution still", currentLive.hasActiveConsumerThread, is(equalTo(false))); // no other thread should still be processing
						
						assertThat("Not enough events consumed: "+currentLive.eventsConsumed, currentLive.eventsConsumed > 10, is(equalTo(true)));
							// reset this consumer
						
						currentLive.eventsConsumed = 0;
						
						if (currentLive == PipelinedEventConsumerUnitTest.this.live){
							 currentLive = PipelinedEventConsumerUnitTest.this.staging;
						} else{
							currentLive =  PipelinedEventConsumerUnitTest.this.live;
						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (Throwable t){
					inError.set(true);
					throw t;
				}
			}
			
		});
		
		thread1.start();
		thread2.start();
		starter.await();
		
		Thread.sleep(5000); // run test for 5 seconds
		
		stopped.set(true);
		
		assertThat(inError.get(), is(equalTo(false)));
	}
	
	private static class TestEvent implements Event{

		private final int currentThread;
		
		public TestEvent(int currentThread) {
			this.currentThread = currentThread;
		}

		@Override
		public Collection<Feature> getFeatures() {
			return Collections.emptyList();
		}
	}
	
	private static class TestEventConsumer implements EventConsumer<TestEvent>{

		private boolean hasActiveConsumerThread;
		private int eventsConsumed;
		private final String name;
		
		private TestEventConsumer(String name){
			this.name = name;
		}
		
		@Override
		public void consume(TestEvent event) {
			hasActiveConsumerThread = true;
			try{
				double total = 0;
				for (int i = 0; i < 45300; i++){ // do some work
					total += Math.random();
				}
				
			}finally{
				eventsConsumed++;
				hasActiveConsumerThread = false;
			}
		}
	}
}
