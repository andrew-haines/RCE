package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class AccumulatorEventConsumer<T extends Event> implements EventConsumer<T>{

	private final int[] accumulators;
	private final AccumulatorSlotLookup<T> lookup;
	
	public AccumulatorEventConsumer(AccumulatorConfig config, AccumulatorSlotLookup<T> lookup){
		accumulators = new int[config.getAccumulatorSlots()];
		this.lookup = lookup;
	}
	@Override
	public void consume(T event) {
		accumulators[lookup.getSlot(event)]++;
	}

}
