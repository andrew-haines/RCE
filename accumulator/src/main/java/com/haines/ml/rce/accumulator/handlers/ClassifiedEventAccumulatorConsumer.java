package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.FeatureHandlerRepository;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;

public class ClassifiedEventAccumulatorConsumer<T extends ClassifiedEvent> extends FeaturedEventAccumulatorEventConsumer<T>{
	
	public ClassifiedEventAccumulatorConsumer(AccumulatorConfig config, AccumulatorLookupStrategy<? super T> lookup, FeatureHandlerRepository<T> featureHandlers) {
		super(config, lookup, featureHandlers);
	}

	@Override
	public void consume(T event) {
		super.consume(event);
		
		for (Classification classification: event.getClassificationsList()){
			getFeatureHandlers().getClassificationHandler(classification.getType()).increment(classification, event, this, this.getLookupStrategy());
		}
	}
}
