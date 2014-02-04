package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;

public interface AccumulatorLookupStrategy<T extends Event> {

	int[] getSlots(T event);
}
