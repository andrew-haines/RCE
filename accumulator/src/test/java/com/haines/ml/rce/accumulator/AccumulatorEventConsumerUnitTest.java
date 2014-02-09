package com.haines.ml.rce.accumulator;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

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
			for (int j = 0; j < 16777216; j++){
				candidate.consume(new TestEvent(new int[]{j}));
			}
		}
		for (int j = 0; j < 16777216; j++){
			assertThat("j="+j, candidate.getAccumulatorValue(j), is(equalTo(3)));
		}
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		
		for (int i = 0; i < 16777216; i++){
			candidate.consume(new TestEvent(new int[]{16777215}));
		}
		
		assertThat(candidate.getAccumulatorValue(16777215), is(equalTo(16777216)));
	}
	
	@Test
	public void givenCandidate_whenConsumingEventThatIndexesToAnUnsupportedSlot_thenEntireEventIsRolledBack(){
		candidate.consume(new TestEvent(new int[]{0, 1}));
		candidate.consume(new TestEvent(new int[]{16777217, 16777216})); // entire event should be ignored
		
		candidate.consume(new TestEvent(new int[]{16777215, 16777210, 3, 16777219})); // entire event should be ignored
		candidate.consume(new TestEvent(new int[]{16777215, 16777215, 16777214}));
		candidate.consume(new TestEvent(new int[]{16777215, 16777214}));
		
		assertThat(candidate.getAccumulatorValue(0), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(16777216), is(equalTo(1))); // only updated once
		assertThat(candidate.getAccumulatorValue(16777217), is(equalTo(0))); // never updated
		assertThat(candidate.getAccumulatorValue(16777210), is(equalTo(0))); // never updated
		assertThat(candidate.getAccumulatorValue(3), is(equalTo(0))); // never updated
		assertThat(candidate.getAccumulatorValue(16777215), is(equalTo(3))); 
		assertThat(candidate.getAccumulatorValue(16777214), is(equalTo(2))); 
	}
	
	@Test
	public void givenCandidate_whenConsumingSlotsPerEvent_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(0)));
		
		candidate.consume(new TestEvent(new int[]{1, 5, 7,8 ,3 ,4}));
		
		assertThat(candidate.getAccumulatorValue(0), is(equalTo(0)));
		assertThat(candidate.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(2), is(equalTo(0)));
		assertThat(candidate.getAccumulatorValue(5), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(7), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(8), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(3), is(equalTo(1)));
		assertThat(candidate.getAccumulatorValue(4), is(equalTo(1)));
	}
	
	private static class TestEvent implements Event{
		
		private final int[] slotsToIncrement;
		
		private TestEvent(int[] slotsToIncrement){
			this.slotsToIncrement = slotsToIncrement;
		}

		public int[] getSlotsToIncrement() {
			return slotsToIncrement;
		}

		@Override
		public Collection<Feature> getFeatures() {
			return Collections.emptyList();
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
