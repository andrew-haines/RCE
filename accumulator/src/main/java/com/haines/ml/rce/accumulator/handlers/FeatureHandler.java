package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.DistributionProvider;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

public interface FeatureHandler<T extends Event> {

	/**
	 * Given the feature and event, this handler will update the accumulator appropriately
	 * @param feature
	 * @param event
	 * @param accumulator
	 */
	void increment(Feature feature, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup);
	
	/**
	 * Returns the distribution provider for this feature handler. If there is no distribution representation for this feature then this
	 * method should return null
	 * @return
	 */
	DistributionProvider getDistributionProvider();
	
	/**
	 * Return the number of accumulator slots required to store this feature.
	 * @return
	 */
	int getNumSlotsRequired();
}
