package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class SyncPipelineEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<E>> implements EventConsumer<E>  {

	private final PipelineAccumulatorController<T> controller;
	private final DisruptorEventConsumer<T> nextStageConsumer;
	private final AccumulatorEventConsumer<E> eventConsumer;
	
	public SyncPipelineEventConsumer(PipelineAccumulatorController<T> controller, AccumulatorEventConsumer<E> eventConsumer, DisruptorEventConsumer<T> nextStageConsumer){
		this.controller = controller;
		this.eventConsumer = eventConsumer;
		this.nextStageConsumer = nextStageConsumer;
	}
	
	@Override
	public void consume(E event) {
		eventConsumer.consume(event);
		
		controller.pushIfRequired(eventConsumer, nextStageConsumer);
	}

	private static class DisruptorEventConsumer<T extends AccumulatorLookupStrategy<?>> implements EventConsumer<AccumulatedEvent<T>>{

		private final DisruptorConsumer<AccumulatedEvent<T>> nextStageConsumer;
		
		private DisruptorEventConsumer(DisruptorConsumer<AccumulatedEvent<T>> nextStageConsumer){
			this.nextStageConsumer = nextStageConsumer;
		}
		
		@Override
		public void consume(AccumulatedEvent<T> event) {
			nextStageConsumer.consumeEvent(event);
		}
		
	}
}
