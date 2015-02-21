package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;

public class ClassifiedEventAccumulatorConsumer<T extends ClassifiedEvent> extends FeaturedEventAccumulatorEventConsumer<T>{
	
	public ClassifiedEventAccumulatorConsumer(AccumulatorConfig config, AccumulatorLookupStrategy<? super T> lookup, HandlerRepository<T> handlers) {
		super(config, lookup, handlers);
	}

	@Override
	public void consume(T event) {
		super.consume(event);
		
		for (Classification classification: event.getClassificationsList()){
			handlers.getClassificationHandler(classification).increment(classification, event, this, lookup);
		}
	}
}
