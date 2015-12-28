package com.haines.ml.rce.accumulator.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.DistributionProvider;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

/**
 * A Feature and Classification handler that uses discrete values of the features to index each accumular slot
 * @author haines
 *
 * @param <T>
 */
public class DiscreteLookupAccumulatorHandler<T extends Event> implements FeatureHandler<T>, ClassificationHandler<T>{

	private final static Logger LOG = LoggerFactory.getLogger(DiscreteLookupAccumulatorHandler.class);
	@Override
	public void increment(Feature feature, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup) {
		
		int[] slots = lookup.getSlots(feature, event);
		
		accumulator.incrementAccumulators(slots);
	}

	@Override
	public void increment(Classification classification, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup) {
		int slot = lookup.getSlot(classification, event);
		
		accumulator.incrementAccumulator(slot);
	}

	@Override
	public DistributionProvider getDistributionProvider() { // no distribution provider for this feature handler
		return null;
	}

	@Override
	public int getNumSlotsRequired() {
		return 1;
	}
}
