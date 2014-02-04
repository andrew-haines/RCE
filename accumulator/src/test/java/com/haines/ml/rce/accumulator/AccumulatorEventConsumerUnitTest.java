package com.haines.ml.rce.accumulator;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.model.Event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class AccumulatorEventConsumerUnitTest {

	private AccumulatorEventConsumer<TestEvent> candidate;
	
	@Before
	public void before(){
		candidate = new AccumulatorEventConsumer<TestEvent>(null, new TestEventAccumulatorLookupStrategy());
	}
	
	@Test
	public void givenCandidate_whenConsumingEvent_thenAccumulatorUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new TestEvent(new int[]{1}));
		
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingTwoEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new TestEvent(new int[]{1}));
		candidate.consume(new TestEvent(new int[]{1}));
		
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(2)));
		assertThat(candidate.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{1}));
		}
		
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{4}));
		}
		
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(10)));
		assertThat(candidate.getAccumulatorValue(4), is(equalTo(10)));
		assertThat(candidate.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAccumulatorLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{1}));
		}
		
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{257}));
		}
		
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(10)));
		assertThat(candidate.getAccumulatorValue(257), is(equalTo(10)));
		assertThat(candidate.getAccumulatorValue(0), is(equalTo(0)));
		assertThat(candidate.getAccumulatorValue(1024), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 65536; j++){
				candidate.consume(new TestEvent(new int[]{j}));
			}
		}
		for (int j = 0; j < 65536; j++){
			assertThat(candidate.getAccumulatorValue(j), is(equalTo(3)));
		}
	}
	
	private static class TestEvent implements Event{
		
		private final int[] slotsToIncrement;
		
		private TestEvent(int[] slotsToIncrement){
			this.slotsToIncrement = slotsToIncrement;
		}

		public int[] getSlotsToIncrement() {
			return slotsToIncrement;
		}
	}
	
	private static class TestEventAccumulatorLookupStrategy implements AccumulatorLookupStrategy<TestEvent>{

		@Override
		public int[] getSlots(TestEvent event) {
			return event.getSlotsToIncrement();
		}
		
	}
}
