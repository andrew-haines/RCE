package com.haines.ml.rce.accumulator.handlers;

import java.util.ArrayList;
import java.util.Collection;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorUnitTest;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.FeatureHandlerRepository;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.FeaturedEvent;
import com.haines.ml.rce.test.TestFeature;

public class FeaturedEventAccumulatorEventConsumerUnitTest extends AccumulatorUnitTest {
	
	@Override
	protected Accumulator<? extends TestEvent> getNewAccumulator(AccumulatorLookupStrategy<? extends TestEvent> lookupStrategy) {
		
		@SuppressWarnings("unchecked")
		AccumulatorLookupStrategy<FeaturedTestEvent> featuredLookupStrategy = (AccumulatorLookupStrategy<FeaturedTestEvent>)lookupStrategy;
		
		return new FeaturedEventAccumulatorEventConsumer<FeaturedTestEvent>(Accumulator.DEFAULT_CONFIG, featuredLookupStrategy, FeatureHandlerRepository.<FeaturedTestEvent>create());
	}

	private static class FeaturedTestEvent extends TestEvent implements FeaturedEvent {

		private final Collection<TestFeature> featureList;
		
		FeaturedTestEvent(int[] slotsToIncrement) {
			super(slotsToIncrement);
			
			this.featureList = new ArrayList<>();
			this.featureList.add(new TestFeature(""));
		}

		@Override
		public Collection<? extends Feature> getFeaturesList() {
			return featureList;
		}
		
	}
	
	protected TestEvent createTestEvent(int[] indexes){
		return new FeaturedTestEvent(indexes);
	}
}
