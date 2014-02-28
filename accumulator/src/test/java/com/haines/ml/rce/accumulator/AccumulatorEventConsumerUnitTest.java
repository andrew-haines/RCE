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
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new TestEvent(new int[]{1}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingTwoEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new TestEvent(new int[]{1}));
		candidate.consume(new TestEvent(new int[]{1}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(2)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{1}));
		}
		
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{4}));
		}
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(4), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAccumulatorLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{1}));
		}
		
		for (int i = 0; i < 10; i++){
			candidate.consume(new TestEvent(new int[]{257}));
		}
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(257), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
		assertThat(provider.getAccumulatorValue(1024), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 16777216; j++){
				candidate.consume(new TestEvent(new int[]{j}));
			}
		}
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		for (int j = 0; j < 16777216; j++){
			assertThat("j="+j, provider.getAccumulatorValue(j), is(equalTo(3)));
		}
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		for (int i = 0; i < 16777216; i++){
			candidate.consume(new TestEvent(new int[]{16777215}));
		}
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(16777215), is(equalTo(16777216)));
	}
	
	@Test
	public void givenCandidate_whenConsumingEventThatIndexesToAnUnsupportedSlot_thenEntireEventIsRolledBack(){
		candidate.consume(new TestEvent(new int[]{0, 1}));
		candidate.consume(new TestEvent(new int[]{16777217, 16777216})); // entire event should be ignored
		
		candidate.consume(new TestEvent(new int[]{16777215, 16777210, 3, 16777219})); // entire event should be ignored
		candidate.consume(new TestEvent(new int[]{16777215, 16777215, 16777214}));
		candidate.consume(new TestEvent(new int[]{16777215, 16777214}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(0), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(16777216), is(equalTo(1))); // only updated once
		assertThat(provider.getAccumulatorValue(16777217), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(16777210), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(3), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(16777215), is(equalTo(3))); 
		assertThat(provider.getAccumulatorValue(16777214), is(equalTo(2))); 
	}
	
	@Test
	public void givenCandidate_whenConsumingSlotsPerEvent_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		candidate.consume(new TestEvent(new int[]{1, 5, 7,8 ,3 ,4}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(2), is(equalTo(0)));
		assertThat(provider.getAccumulatorValue(5), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(7), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(8), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(3), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(4), is(equalTo(1)));
	}
	
	@Test
	public void givenCandidate_whenConsumingEvent_thenUnderlyingAccumulatorProviderRemainsUnchanged(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new TestEvent(new int[]{1}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
		
		candidate.consume(new TestEvent(new int[]{1}));
		candidate.consume(new TestEvent(new int[]{1}));
		candidate.consume(new TestEvent(new int[]{1}));
		candidate.consume(new TestEvent(new int[]{1}));
		
		// the original provider should remain unchanged
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
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

		@Override
		public int getMaxIndex() {
			return 16777216;
		}
		
	}
}
