package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;

/**
 * A provider interface for obtaining a reference to an underlying accumulator.
 * @author haines
 *
 * @param <E>
 */
public interface AccumulatorProvider<E extends Event> {

	/**
	 * Returns the accumulator value at a particular slot.
	 * @param slot
	 * @return
	 */
	int getAccumulatorValue(int slot);
	
	/**
	 * Returns the accumulator value at a particular slot but as a floating point number
	 * @param slot
	 * @return
	 */
	float getAccumulatorValueAsFloat(int slot);
	
	/**
	 * Returns the strategy that links to this accumulator
	 * @return
	 */
	AccumulatorLookupStrategy<? super E> getLookupStrategy();
}
