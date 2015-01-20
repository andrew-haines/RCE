package com.haines.ml.rce.dispatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;


public class DisruptorDispatcherConsumerITest {
	
	private static final Logger LOG = LoggerFactory.getLogger(DisruptorDispatcherConsumerITest.class);

	private static final String TEST_EVENT_STRING = "testString";

	private static final int NUM_TEST_EVENTS = 5000000;
	
	private Dispatcher<Event> candidate;
	private Iterable<TestEventConsumer> consumers;
	private CountDownLatch consumerLatch;
	
	@Before
	public void before(){
		
		before(1);
	}
	
	public void before(int numEventExpected){
		
		consumerLatch = new CountDownLatch(numEventExpected);
		
		consumers = Arrays.asList(new TestEventConsumer(consumerLatch));
		
		before(consumers);
	}
	
	public void before(Iterable<TestEventConsumer> consumers){
		
		candidate = new Dispatcher<Event>(getTestConsumers(consumers));
	}
	
	@Test
	public void givenSingleConsumerCandidate_whenAddingEvent_thenEventConsumed() throws InterruptedException{
		candidate.dispatchEvent(new TestEvent(TEST_EVENT_STRING, 1));
		
		consumerLatch.await();
		
		TestEventConsumer consumer = Iterables.get(consumers, 0);
		
		assertThat(consumer.getEventsRecieved().size(), is(equalTo(1)));
		
		assertThat(Iterables.get(consumer.getEventsRecieved(), 0).testString, is(equalTo(TEST_EVENT_STRING)));
		assertThat(Iterables.get(consumer.getEventsRecieved(), 0).testNum, is(equalTo(1)));
		assertThat(consumer.getNumHeartBeatsRecieved(), is(equalTo(0)));
	}
	
	@Test
	public void givenSingleConsumerCandidate_whenAddingMultipleEvents_thenEventsConsumed() throws InterruptedException{
		
		before(NUM_TEST_EVENTS);
		
		candidate.dispatchEvent(new TestEvent(TEST_EVENT_STRING+0, 0));
		
		long timeStarted = System.currentTimeMillis();
		for (int i = 1; i < NUM_TEST_EVENTS; i++){
			candidate.dispatchEvent(new TestEvent(TEST_EVENT_STRING+i, i));
		}
		
		consumerLatch.await();
		
		long runTime = System.currentTimeMillis() - timeStarted;
		
		LOG.debug(NUM_TEST_EVENTS+" events processed in "+ runTime+" ms - "+calculateRPS(runTime, NUM_TEST_EVENTS)+" rps");
		
		TestEventConsumer consumer = Iterables.get(consumers, 0);
		
		assertThat(consumer.getEventsRecieved().size(), is(equalTo(NUM_TEST_EVENTS)));
		
		int i = 0;
		for (TestEvent event: consumer.getEventsRecieved()){
			assertThat(event.testString, is(equalTo(TEST_EVENT_STRING+i)));
			assertThat(event.testNum, is(equalTo(i)));
			assertThat(consumer.getNumHeartBeatsRecieved(), is(equalTo(0)));
			i++;
		}
	}
	
	@Test
	public void givenMultipleConsumerCandidate_whenAddingMultipleEvents_thenEventsConsumedOverAllConsumers() throws InterruptedException{
		
		consumerLatch = new CountDownLatch(NUM_TEST_EVENTS);
		Collection<TestEventConsumer> consumers = new ArrayList<TestEventConsumer>();
		
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		
		before(consumers);
		
		candidate.dispatchEvent(new TestEvent(TEST_EVENT_STRING+0, 0));
		
		long timeStarted = System.currentTimeMillis();
		for (int i = 1; i < NUM_TEST_EVENTS; i++){
			candidate.dispatchEvent(new TestEvent(TEST_EVENT_STRING+i, i));
		}
		
		consumerLatch.await();
		
		long runTime = System.currentTimeMillis() - timeStarted;
		
		LOG.debug(NUM_TEST_EVENTS+" events processed in "+ runTime+" ms - "+calculateRPS(runTime, NUM_TEST_EVENTS)+" rps");
		
		int totalEvents = 0;
		
		for (TestEventConsumer consumer: consumers){
			totalEvents += consumer.getEventsRecieved().size();
			
			assertThat(consumer.getEventsRecieved().size() > 0, is(equalTo(true)));
			for (TestEvent event: consumer.getEventsRecieved()){
				assertThat(event.testString, is(not(nullValue())));
				assertThat(event.testNum >= 0, is(equalTo(true)));
			}
			assertThat(consumer.getNumHeartBeatsRecieved(), is(equalTo(0)));
		}
		
		assertThat(totalEvents, is(equalTo(NUM_TEST_EVENTS)));
	}
	
	@Test
	public void givenMultipleConsumerCandidates_whenSendingHeartBeat_thenAllConsumersNotified() throws InterruptedException{
		
		consumerLatch = new CountDownLatch(7);
		Collection<TestEventConsumer> consumers = new ArrayList<TestEventConsumer>();
		
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		consumers.add(new TestEventConsumer(consumerLatch));
		
		before(consumers);
		
		candidate.sendHeartBeat();
		
		consumerLatch.await();
		
		
		for (TestEventConsumer consumer: consumers){
			assertThat(consumer.getNumHeartBeatsRecieved(), is(equalTo(1)));
		}
	}
	
	private static double calculateRPS(long timeSpent, int numberEventsToSend) {
		
		return 1000 / ((double)timeSpent / numberEventsToSend);
	}

	private Iterable<DispatcherConsumer<Event>> getTestConsumers(Iterable<TestEventConsumer> consumers) {
		
		Iterable<DispatcherConsumer<Event>> builders = Iterables.transform(consumers, new Function<TestEventConsumer, DispatcherConsumer<Event>>(){

			@Override
			public DispatcherConsumer<Event> apply(TestEventConsumer input) {
				return new DisruptorConsumer.Builder<Event>(Executors.newSingleThreadExecutor(), 
						new DisruptorConfig.Builder()
							.ringSize(1024)
							.build()
				).addConsumer(input)
				.build();
			}
		});
		
		return builders;
	}

	private static class TestEvent implements Event{
		
		private final String testString;
		private final int testNum;
		
		private TestEvent(String testString, int testNum){
			this.testString = testString;
			this.testNum = testNum;
		}
	}
	
	private static class TestEventConsumer implements EventConsumer<Event>{

		private final Collection<TestEvent> eventsRecieved = new ArrayList<TestEvent>();
		private int numHeartBeatsRecieved = 0;
		private final CountDownLatch latch;
		
		private TestEventConsumer(CountDownLatch latch){
			this.latch = latch;
		}
		
		@Override
		public void consume(Event event) {
			
			if (event == Event.HEARTBEAT){
				setNumHeartBeatsRecieved(getNumHeartBeatsRecieved() + 1);
			} else{
				eventsRecieved.add((TestEvent)event);
			}
			latch.countDown();
		}

		public Collection<TestEvent> getEventsRecieved() {
			return eventsRecieved;
		}

		public int getNumHeartBeatsRecieved() {
			return numHeartBeatsRecieved;
		}

		public void setNumHeartBeatsRecieved(int numHeartBeatsRecieved) {
			this.numHeartBeatsRecieved = numHeartBeatsRecieved;
		}
		
	}
}
