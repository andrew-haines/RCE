package com.haines.ml.rce.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DisruptorDispatcherConsumerITest {

	private static final String TEST_EVENT_STRING = "testString";
	
	private Dispatcher<TestEvent> candidate;
	private TestEventConsumer consumer;
	private CountDownLatch consumerLatch;
	
	@Before
	public void before(){
		before(1);
	}
	
	public void before(int numEventExpected){
		
		consumerLatch = new CountDownLatch(numEventExpected);
		
		consumer = new TestEventConsumer(consumerLatch);
		
		candidate = new Dispatcher<TestEvent>(getTestConsumers(consumer));
	}
	
	@Test
	public void givenSingleConsumerCandidate_whenAddingEvent_thenEventConsumed() throws InterruptedException{
		candidate.dispatchEvent(new TestEvent(TEST_EVENT_STRING, 1));
		
		consumerLatch.await();
		
		assertThat(consumer.getEventsRecieved().size(), is(equalTo(1)));
		
		assertThat(Iterables.get(consumer.getEventsRecieved(), 0).testString, is(equalTo(TEST_EVENT_STRING)));
		assertThat(Iterables.get(consumer.getEventsRecieved(), 0).testNum, is(equalTo(1)));
	}
	
	private Iterable<DispatcherConsumer<TestEvent>> getTestConsumers(TestEventConsumer consumer) {
		
		return Arrays.<DispatcherConsumer<TestEvent>>asList(new DisruptorConsumer.Builder<TestEvent>(Executors.newSingleThreadExecutor(), 
				new DisruptorConfig.Builder()
					.ringSize(1024)
					.build()
				).addConsumer(consumer)
				.build());
	}

	private static class TestEvent implements Event{
		
		private final String testString;
		private final int testNum;
		
		private TestEvent(String testString, int testNum){
			this.testString = testString;
			this.testNum = testNum;
		}
	}
	
	private static class TestEventConsumer implements EventConsumer<TestEvent>{

		private final Collection<TestEvent> eventsRecieved = new ArrayList<TestEvent>();
		private final CountDownLatch latch;
		
		private TestEventConsumer(CountDownLatch latch){
			this.latch = latch;
		}
		
		@Override
		public void consume(TestEvent event) {
			this.getEventsRecieved().add(event);
			latch.countDown();
		}

		public Collection<TestEvent> getEventsRecieved() {
			return eventsRecieved;
		}
		
	}
}
