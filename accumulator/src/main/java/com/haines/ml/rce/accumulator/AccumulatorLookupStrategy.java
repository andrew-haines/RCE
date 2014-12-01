package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;

public interface AccumulatorLookupStrategy<T extends Event> {

	int[] getSlots(T event);
	
	int getMaxIndex();
	
	AccumulatorLookupStrategy<T> copy();
	
	void clear();
	
	public static interface AccumulatorLookupStrategyFactory<E extends Event>{
		
		AccumulatorLookupStrategy<? super E> create();
	}
}
