package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface PosteriorAccumulatorLookupStrategy {

	int[] getPosteriorSlots(Feature feature, Classification classification, int numSlots);
}
