package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

/**
 * A lookup strategy that returns the slots that map to a given feature/classification pairing.
 * @author haines
 *
 */
public interface PosteriorAccumulatorLookupStrategy {

	int[] getPosteriorSlots(Feature feature, Classification classification, int numSlots);
}
