package com.haines.ml.rce.accumulator.model;


import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.model.Event;

public class AccumulatedEvent implements Event {

	private final AccumulatorProvider provider;
	
	public AccumulatedEvent(AccumulatorProvider provider) {
		this.provider = provider;
	}

	public AccumulatorProvider getAccumulatorProvider(){
		return provider;
	}

}
