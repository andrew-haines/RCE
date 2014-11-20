package com.haines.ml.rce.accumulator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.accumulator.AccumulatorEventConsumerUnitTest.TestEvent;

public class CustomAccumulatorEventConsumerUnitTest {
	
	private static final AccumulatorConfig TEST_CONFIG = new AccumulatorConfig() {
		
		@Override
		public int getSecondAccumulatorLineBitDepth() {
			return 4;
		}
		
		@Override
		public int getFirstAccumulatorLineBitDepth() {
			return 4;
		}
		
		@Override
		public int getFinalAccumulatorLineBitDepth() {
			return 4;
		}
	};
	
	private static final AccumulatorConfig LARGE_TEST_CONFIG = new AccumulatorConfig() {
		
		@Override
		public int getSecondAccumulatorLineBitDepth() {
			return 6;
		}
		
		@Override
		public int getFirstAccumulatorLineBitDepth() {
			return 6;
		}
		
		@Override
		public int getFinalAccumulatorLineBitDepth() {
			return 12;
		}
	};
	
	private AccumulatorEventConsumer<AccumulatorEventConsumerUnitTest.TestEvent> candidate;
	private AccumulatorEventConsumer<AccumulatorEventConsumerUnitTest.TestEvent> candidateLarge;
	
	@Before
	public void before(){
		candidate = new AccumulatorEventConsumer<AccumulatorEventConsumerUnitTest.TestEvent>(TEST_CONFIG, new AccumulatorEventConsumerUnitTest.TestEventAccumulatorLookupStrategy());
		candidateLarge = new AccumulatorEventConsumer<AccumulatorEventConsumerUnitTest.TestEvent>(LARGE_TEST_CONFIG, new AccumulatorEventConsumerUnitTest.TestEventAccumulatorLookupStrategy());

	}
	
	@Test
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingSlotsPerEvent_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		candidate.consume(new AccumulatorEventConsumerUnitTest.TestEvent(new int[]{1, 5, 7,8 ,3 ,4}));
		
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
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingEvent_thenAccumulatorUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new AccumulatorEventConsumerUnitTest.TestEvent(new int[]{1}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingTwoEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(new TestEvent(new int[]{1}));
		candidate.consume(new TestEvent(new int[]{1}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(2)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingMultipleEvents_thenAccumulatorsUpdatedCorrectly(){
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
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingMultipleEventsAcrossAccumulatorLines_thenAccumulatorsUpdatedCorrectly(){
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
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingMultipleEventsAcrossAllLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 3; i++){
			for (int j = 0; j < 4096; j++){
				candidate.consume(new TestEvent(new int[]{j}));
			}
		}
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		for (int j = 0; j < 4096; j++){
			assertThat("j="+j, provider.getAccumulatorValue(j), is(equalTo(3)));
		}
	}
	
	@Test
	public void givenCandidateWithConfiguredAccumulatorSizese_whenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		for (int i = 0; i < 4096; i++){
			candidate.consume(new TestEvent(new int[]{4095}));
		}
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(4095), is(equalTo(4096)));
	}
	
	@Test
	public void givenCandidateWithConfiguredAccumulatorSizes_whenConsumingEventThatIndexesToAnUnsupportedSlot_thenEntireEventIsRolledBack(){
		candidate.consume(new TestEvent(new int[]{0, 1}));
		candidate.consume(new TestEvent(new int[]{4096, 4095})); // entire event should be ignored
		
		candidate.consume(new TestEvent(new int[]{4095, 4090, 3, 4099})); // entire event should be ignored
		candidate.consume(new TestEvent(new int[]{2095, 4095, 4094}));
		candidate.consume(new TestEvent(new int[]{4095, 4094}));
		
		AccumulatorProvider provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(0), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(4095), is(equalTo(2))); // only updated twice
		assertThat(provider.getAccumulatorValue(4090), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(2095), is(equalTo(1))); // only updated once
		assertThat(provider.getAccumulatorValue(3), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(4094), is(equalTo(2))); 
	}
	
	@Test
	public void givenCandidateWithLargeConfiguredAccumulatorSizese_whenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidateLarge.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		for (int i = 0; i < 16777216; i++){
			for (int j = 0; j < 10; j++){
				candidateLarge.consume(new TestEvent(new int[]{i}));
			}
		}
		
		AccumulatorProvider provider = candidateLarge.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(16777215), is(equalTo(10)));
	}
	
	@Test
	public void givenCandidateWithLargeConfiguredAccumulatorSizes_whenClearedAndThenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidateLarge.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		for (int i = 0; i < 4096; i++){
			for (int j = 0; j < 10; j++){
				candidateLarge.consume(new TestEvent(new int[]{i}));
			}
		}
		
		AccumulatorProvider provider = candidateLarge.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(4095), is(equalTo(10)));
		candidateLarge.clear();
		
		provider = candidateLarge.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(4095), is(equalTo(0)));
		
		for (int i = 0; i < 4096; i++){
			for (int j = 0; j < 10; j++){
				candidateLarge.consume(new TestEvent(new int[]{i}));
			}
		}
		
		provider = candidateLarge.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(4095), is(equalTo(10)));
	}
}
	
