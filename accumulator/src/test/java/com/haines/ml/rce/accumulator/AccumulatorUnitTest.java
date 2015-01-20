package com.haines.ml.rce.accumulator;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class AccumulatorUnitTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(AccumulatorUnitTest.class);

	private Accumulator<TestEvent> candidate;
	private final AccumulatorLookupStrategy<? extends TestEvent> lookupStrategy;
	
	public AccumulatorUnitTest(){
		this(new TestEventAccumulatorLookupStrategy());
	}
	
	protected AccumulatorUnitTest(AccumulatorLookupStrategy<? extends TestEvent> lookupStrategy){
		this.lookupStrategy = lookupStrategy;
	}
	
	@SuppressWarnings("unchecked")
	@Before
	public void before(){
		
		candidate = (Accumulator<TestEvent>)getNewAccumulator(lookupStrategy);
	}
	
	@SuppressWarnings("unchecked")
	protected Accumulator<? extends TestEvent> getNewAccumulator(AccumulatorLookupStrategy<? extends TestEvent> lookupStrategy) {
		return AccumulatorUnitTest.createNewAccumulator((AccumulatorLookupStrategy<TestEvent>)lookupStrategy);
	}
	
	static Accumulator<TestEvent> createNewAccumulator(AccumulatorLookupStrategy<TestEvent> lookupStrategy){
		return new Accumulator<TestEvent>(lookupStrategy){

			@Override
			public void consume(TestEvent event) {
				
				this.incrementAccumulators(this.getLookupStrategy().getSlots(null, event));
			}
		};
	}
	static Accumulator<TestEvent> createNewAccumulator(AccumulatorConfig config, AccumulatorLookupStrategy<TestEvent> lookupStrategy){
		return new Accumulator<TestEvent>(config, lookupStrategy){

			@Override
			public void consume(TestEvent event) {
				this.incrementAccumulators(this.getLookupStrategy().getSlots(null, event));
			}
		};
	}

	@Test
	public void givenCandidate_whenConsumingEvent_thenAccumulatorUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(createTestEvent(new int[]{1}));
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	protected TestEvent createTestEvent(int[] indexes){
		return new TestEvent(indexes);
	}
	
	@Test
	public void givenCandidate_whenConsumingTwoEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		candidate.consume(createTestEvent(new int[]{1}));
		candidate.consume(createTestEvent(new int[]{1}));
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(2)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEvents_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 10; i++){
			candidate.consume(createTestEvent(new int[]{1}));
		}
		
		for (int i = 0; i < 10; i++){
			candidate.consume(createTestEvent(new int[]{4}));
		}
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(4), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAccumulatorLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 10; i++){
			candidate.consume(createTestEvent(new int[]{1}));
		}
		
		for (int i = 0; i < 10; i++){
			candidate.consume(createTestEvent(new int[]{257}));
		}
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(257), is(equalTo(10)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
		assertThat(provider.getAccumulatorValue(1024), is(equalTo(0)));
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		for (int i = 0; i < 3; i++){
			for (int j = 4096; j < 16777216; j++){
				candidate.consume(createTestEvent(new int[]{j}));
			}
		}
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		for (int j = 4096; j < 16777216; j++){
			assertThat("j="+j, provider.getAccumulatorValue(j), is(equalTo(3)));
		}
	}
	
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		for (int i = 0; i < 16777216; i++){
			candidate.consume(createTestEvent(new int[]{16777215}));
		}
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(16777215), is(equalTo(16777216)));
	}
	
	@Test
	public void givenCandidate_whenConsumingEventThatIndexesToAnUnsupportedSlot_thenEntireEventIsRolledBack(){
		candidate.consume(createTestEvent(new int[]{0, 1}));
		candidate.consume(createTestEvent(new int[]{16777217, 16777216})); // entire event should be ignored
		
		candidate.consume(createTestEvent(new int[]{16777215, 16777210, 3, 16777219})); // entire event should be ignored
		candidate.consume(createTestEvent(new int[]{16777215, 16777215, 16777214}));
		candidate.consume(createTestEvent(new int[]{16777215, 16777214}));
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(0), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(16777216), is(equalTo(0))); // only updated once
		assertThat(provider.getAccumulatorValue(16777217), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(16777210), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(3), is(equalTo(0))); // never updated
		assertThat(provider.getAccumulatorValue(16777215), is(equalTo(3))); 
		assertThat(provider.getAccumulatorValue(16777214), is(equalTo(2))); 
	}
	
	@Test
	public void givenCandidate_whenConsumingSlotsPerEvent_thenAccumulatorsUpdatedCorrectly(){
		assertThat(candidate.getAccumulatorProvider().getAccumulatorValue(1), is(equalTo(0)));
		
		candidate.consume(createTestEvent(new int[]{1, 5, 7,8 ,3 ,4}));
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
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
		candidate.consume(createTestEvent(new int[]{1}));
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
		
		candidate.consume(createTestEvent(new int[]{1}));
		candidate.consume(createTestEvent(new int[]{1}));
		candidate.consume(createTestEvent(new int[]{1}));
		candidate.consume(createTestEvent(new int[]{1}));
		
		// the original provider should remain unchanged
		
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(0), is(equalTo(0)));
	}
	
	protected static class TestEvent implements Event{
		
		private final int[] slotsToIncrement;
		
		protected TestEvent(int[] slotsToIncrement){
			this.slotsToIncrement = slotsToIncrement;
		}

		public int[] getSlotsToIncrement() {
			return slotsToIncrement;
		}

	}
	
	protected static class TestEventAccumulatorLookupStrategy implements AccumulatorLookupStrategy<TestEvent>{

		@Override
		public int[] getSlots(Feature feature, TestEvent event) {
			return event.getSlotsToIncrement();
		}

		@Override
		public int getMaxIndex() {
			return 16777218;
		}

		@Override
		public void clear() {
			// NoOp
		}

		@Override
		public AccumulatorLookupStrategy<TestEvent> copy() {
			return null;
		}

		@Override
		public int getSlot(Classification classification, TestEvent event) {
			return -1;
		}

		@Override
		public int[] getPosteriorSlots(Feature feature,
				Classification classification, int numSlots) {
			return null;
		}

		@Override
		public int[] getClassificationSlots(Classification classification, int numSlots) {
			return null;
		}

		@Override
		public int getClassificationSlot(Classification classification) {
			return 0;
		}
	}
}