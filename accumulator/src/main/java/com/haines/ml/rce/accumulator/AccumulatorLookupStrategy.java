package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.accumulator.handlers.ClassificationAccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.handlers.PosteriorAccumulatorLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

public interface AccumulatorLookupStrategy<T extends Event> extends PosteriorAccumulatorLookupStrategy, ClassificationAccumulatorLookupStrategy{

	int[] getSlots(Feature feature, T event);
	
	int getSlot(Classification classification, T event);
	
	int getMaxIndex();
	
	AccumulatorLookupStrategy<T> copy();
	
	void clear();
	
	public static interface AccumulatorLookupStrategyFactory<E extends Event>{
		
		AccumulatorLookupStrategy<? super E> create();
	}
}
