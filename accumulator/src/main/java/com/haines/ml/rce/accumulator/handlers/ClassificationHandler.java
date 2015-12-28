package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.DistributionProvider;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;

/**
 * A classification handler is one that determines how an accumulator should be updated given a particular classification, event and strategy
 * @author haines
 *
 * @param <T>
 */
public interface ClassificationHandler<T extends Event> {

	/**
	 * Given the classification and event, increment the accumulator appropriately
	 * @param classification
	 * @param event
	 * @param accumulator
	 */
	void increment(Classification classification, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup);

	/**
	 * Returns the distribution provider for this classification. If there is no distribution modelling for this classification (such as using discrete
	 * values) then this should return null.
	 * @return
	 */
	DistributionProvider getDistributionProvider();
	
	/**
	 * Return the number of accumulator slots required to store this feature.
	 * @return
	 */
	int getNumSlotsRequired();
}
