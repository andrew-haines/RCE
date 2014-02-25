package com.haines.ml.rce.accumulator;

public interface Accumulator {

	/**
	 * Returns the accumulator value at a particular slot.
	 * @param slot
	 * @return
	 */
	int getAccumulatorValue(int slot);
}
