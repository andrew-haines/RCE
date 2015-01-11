package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.model.Classification;

public interface ClassificationAccumulatorLookupStrategy {

	/**
	 * Returns an array of length numSlots that represent the accumulator slots for this classification. Normally
	 * you would just want 1 slot to update (the count of occurances of this classification) but by having multiple
	 * slots possible, classification values can share slots (such as defining gaussian pdfs)
	 * 
	 * @param classification
	 * @param numSlots
	 * @return
	 */
	int[] getClassificationSlots(Classification classification, int numSlots);
	
	/**
	 * Returns the slot for updating the count of this classification value in the accumulator.
	 * 
	 * @param classification
	 * @return
	 */
	int getClassificationSlot(Classification classification);
}
