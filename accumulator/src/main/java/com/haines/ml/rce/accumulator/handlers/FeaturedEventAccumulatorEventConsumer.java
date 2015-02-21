package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.FeaturedEvent;

public class FeaturedEventAccumulatorEventConsumer<T extends FeaturedEvent> extends Accumulator<T> implements EventConsumer<T>{

	protected final HandlerRepository<T> handlers;
	
	public FeaturedEventAccumulatorEventConsumer(AccumulatorConfig config,AccumulatorLookupStrategy<? super T> lookup, HandlerRepository<T> handlers) {
		super(config, lookup);
		this.handlers = handlers;
	}

	@Override
	public void consume(T event) {
		
		for (Feature feature: event.getFeaturesList()){
			handlers.getFeatureHandler(feature).increment(feature, event, this, lookup);
		}
	}

	protected final HandlerRepository<T> getFeatureHandlers() {
		return handlers;
	}
}
