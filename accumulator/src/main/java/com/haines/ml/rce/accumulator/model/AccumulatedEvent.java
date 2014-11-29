package com.haines.ml.rce.accumulator.model;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.model.Event;

public class AccumulatedEvent<T extends AccumulatorLookupStrategy<?>> implements Event {

	private final AccumulatorProvider<?> provider;
	private final T lookupStrategy;
	
	public AccumulatedEvent(AccumulatorProvider<?> provider, T lookupStrategy) {
		this.provider = provider;
		this.lookupStrategy = lookupStrategy;
	}

	public AccumulatorProvider<?> getAccumulatorProvider(){
		return provider;
	}

	public T getLookupStrategy() {
		return lookupStrategy;
	}
}
