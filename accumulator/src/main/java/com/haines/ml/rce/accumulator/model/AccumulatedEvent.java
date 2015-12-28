package com.haines.ml.rce.accumulator.model;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.model.Event;

/**
 * An event that represents an accumulation (using the {@link Accumulator} class) of events used for downstream processing
 * @author haines
 *
 * @param <T>
 */
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
