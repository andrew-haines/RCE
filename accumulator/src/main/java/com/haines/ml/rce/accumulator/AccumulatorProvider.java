package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;

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
	
	AccumulatorLookupStrategy<? super E> getLookupStrategy();
}
