package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class SyncPipelineEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<E>> implements EventConsumer<E>  {

	private final PipelineAccumulatorController<T> controller;
	private final EventConsumer<AccumulatedEvent<T>> nextStageConsumer;
	private final AccumulatorEventConsumer<E> eventConsumer;
	
	public SyncPipelineEventConsumer(PipelineAccumulatorController<T> controller, AccumulatorEventConsumer<E> eventConsumer, EventConsumer<AccumulatedEvent<T>> nextStageConsumer){
		this.controller = controller;
		this.eventConsumer = eventConsumer;
		this.nextStageConsumer = nextStageConsumer;
	}
	
	@Override
	public void consume(E event) {
		eventConsumer.consume(event);
		
		controller.pushIfRequired(eventConsumer, nextStageConsumer);
	}

}
