package com.haines.ml.rce.accumulator;

import javax.inject.Inject;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class SyncPipelineEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<? super E>> implements EventConsumer<E>  {

	private final PipelineAccumulatorController controller;
	private final EventConsumer<AccumulatedEvent<T>> nextStageConsumer;
	private final AccumulatorEventConsumer<E> eventConsumer;
	
	@Inject
	public SyncPipelineEventConsumer(PipelineAccumulatorController controller, AccumulatorEventConsumer<E> eventConsumer, EventConsumer<AccumulatedEvent<T>> nextStageConsumer){
		this.controller = controller;
		this.eventConsumer = eventConsumer;
		this.nextStageConsumer = nextStageConsumer;
	}
	
	@Override
	public void consume(E event) {
		eventConsumer.consume(event);
		
		controller.pushIfRequired(eventConsumer, nextStageConsumer);
	}

	public static class DisruptorEventConsumer<T extends AccumulatorLookupStrategy<?>> implements EventConsumer<AccumulatedEvent<T>>{

		private final DisruptorConsumer<AccumulatedEvent<T>> nextStageConsumer;
		
		public DisruptorEventConsumer(DisruptorConsumer<AccumulatedEvent<T>> nextStageConsumer){
			this.nextStageConsumer = nextStageConsumer;
		}
		
		@Override
		public void consume(AccumulatedEvent<T> event) {
			nextStageConsumer.consumeEvent(event);
		}
		
	}
}
