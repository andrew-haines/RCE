package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.FeatureHandlerRepository;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.FeaturedEvent;

public class FeaturedEventAccumulatorEventConsumer<T extends FeaturedEvent> extends Accumulator<T> implements EventConsumer<T>{

	protected final FeatureHandlerRepository<T> featureHandlers;
	
	public FeaturedEventAccumulatorEventConsumer(AccumulatorConfig config,AccumulatorLookupStrategy<? super T> lookup, FeatureHandlerRepository<T> featureHandlers) {
		super(config, lookup);
		this.featureHandlers = featureHandlers;
	}

	@Override
	public void consume(T event) {
		
		for (Feature feature: event.getFeaturesList()){
			featureHandlers.getFeatureHandler(feature.getType()).increment(feature, event, this, lookup);
		}
	}

	protected final FeatureHandlerRepository<T> getFeatureHandlers() {
		return featureHandlers;
	}
}
