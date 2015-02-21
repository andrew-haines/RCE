package com.haines.ml.rce.accumulator.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.TestClassification;

public class ClassifiedEventAccumulatorConsumerUnitTest extends FeaturedEventAccumulatorEventConsumerUnitTest{

	@Override
	protected Accumulator<? extends TestEvent> getNewAccumulator(AccumulatorLookupStrategy<? extends TestEvent> lookupStrategy) {
		
		@SuppressWarnings("unchecked")
		AccumulatorLookupStrategy<ClassifiedTestEvent> featuredLookupStrategy = (AccumulatorLookupStrategy<ClassifiedTestEvent>)lookupStrategy;
		
		return new ClassifiedEventAccumulatorConsumer<ClassifiedTestEvent>(Accumulator.DEFAULT_CONFIG, featuredLookupStrategy, HandlerRepository.<ClassifiedTestEvent>create());
	}
	
	@Override
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines_thenAccumulatorsUpdatedCorrectly(){
		// no op. This test is not valid given that the classifications are now also updated, so for each event, 2 slots are updated
	}
	
	@Override
	@Test
	public void givenCandidate_whenConsumingMultipleEventsAcrossAllLines2_thenAccumulatorsUpdatedCorrectly(){
		// no op. This test is not valid given that the classifications are now also updated, so for each event, 2 slots are updated
	}
	
	@Override
	@Test
	public void givenCandidate_whenConsumingEventThatIndexesToAnUnsupportedSlot_thenEntireEventIsRolledBack(){
		// no op. This test is not valid given that the classifications are now also updated, so for each event, 2 slots are updated
	}
	
	@Test
	public void givenCandidate_whenConsumingEvent_then2SlotsUpdatedForFeatureAndClassification(){
		candidate.consume(createTestEvent(new int[]{0, 1}, 2));
		
		AccumulatorProvider<TestEvent> provider = candidate.getAccumulatorProvider();
		
		assertThat(provider.getAccumulatorValue(0), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(1), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(2), is(equalTo(1)));
		assertThat(provider.getAccumulatorValue(3), is(equalTo(0)));
	}
	
	private static final class ClassifiedTestEvent extends FeaturedTestEvent implements ClassifiedEvent{

		private final Collection<TestClassification> classificationList;
		
		protected ClassifiedTestEvent(int[] slotsToIncrement, int classificationSlot) {
			super(slotsToIncrement, classificationSlot);
			
			this.classificationList = new ArrayList<>();
			this.classificationList.add(new TestClassification(""));
		}

		@Override
		public Collection<? extends Classification> getClassificationsList() {
			return classificationList;
		}
		
	}

	@Override
	protected TestEvent createTestEvent(int[] indexes, int classificationSlot) {
		return new ClassifiedTestEvent(indexes, classificationSlot);
	}

	
}
