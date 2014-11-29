package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;

public interface AccumulatorProvider<E extends Event> {

	/**
	 * Returns the accumulator value at a particular slot.
	 * @param slot
	 * @return
	 */
	int getAccumulatorValue(int slot);
	
	AccumulatorLookupStrategy<? super E> getLookupStrategy();
}
