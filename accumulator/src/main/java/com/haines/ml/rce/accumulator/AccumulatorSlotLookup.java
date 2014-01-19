package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;

public interface AccumulatorSlotLookup<T extends Event> {

	int getSlot(T event);
}
