package com.haines.ml.rce.accumulator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class SyncPipelineEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<? super E>> implements EventConsumer<E>  {

	private static final Logger LOG = LoggerFactory.getLogger(SyncPipelineEventConsumer.class);
	
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
		if (event != Event.HEARTBEAT){ // dont actually send heart beat to consumer. Only send it to the controller.
			eventConsumer.consume(event);
		} else{
			LOG.info("Recieved heart beat.");
		}
		
		controller.pushIfRequired(eventConsumer, nextStageConsumer);
	}

	public static class DisruptorEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<E>> implements EventConsumer<AccumulatedEvent<T>>{

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
