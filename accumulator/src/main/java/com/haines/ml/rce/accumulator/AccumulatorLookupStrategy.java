package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.accumulator.handlers.ClassificationAccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.handlers.PosteriorAccumulatorLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

/**
 * An interface that maps events and features to a given slot to be used for accumulation in the accumulator.
 * @author haines
 *
 * @param <T>
 */
public interface AccumulatorLookupStrategy<T extends Event> extends PosteriorAccumulatorLookupStrategy, ClassificationAccumulatorLookupStrategy{

	/**
	 * Given a feature and an event that contains this feature, return a number of slots that can be used to represent this feature's
	 * accumulated value in the model.
	 * @param feature
	 * @param event
	 * @return
	 */
	int[] getSlots(Feature feature, T event);
	
	/**
	 * Given a classification and an event, return the slot that this maps to.
	 * @param classification
	 * @param event
	 * @return
	 */
	int getSlot(Classification classification, T event);
	
	/**
	 * Returns the current max slot index of all features and classifications used in this strategy.
	 * @return
	 */
	int getMaxIndex();
	
	/**
	 * Provide a deep clone copy of this accumulator
	 * @return
	 */
	AccumulatorLookupStrategy<T> copy();
	
	/**
	 * Clear all memory structures for this strategy,
	 */
	void clear();
	
	public static interface AccumulatorLookupStrategyFactory<E extends Event>{
		
		AccumulatorLookupStrategy<? super E> create();
	}
}
