package com.haines.ml.rce.accumulator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.system.SystemStoppedListener;

public class SyncPipelineEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<? super E>> implements EventConsumer<E>, SystemStoppedListener  {

	private static final Logger LOG = LoggerFactory.getLogger(SyncPipelineEventConsumer.class);
	
	private final PipelineAccumulatorController controller;
	private final EventConsumer<AccumulatedEvent<T>> nextStageConsumer;
	private final Accumulator<E> eventConsumer;
	
	@Inject
	public SyncPipelineEventConsumer(PipelineAccumulatorController controller, Accumulator<E> eventConsumer, EventConsumer<AccumulatedEvent<T>> nextStageConsumer){
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
	
	@Override
	public void systemStopped() {
		eventConsumer.clear();
	}

	public static class DisruptorEventConsumer<E extends Event, T extends AccumulatorLookupStrategy<E>> implements EventConsumer<AccumulatedEvent<T>>, SystemStoppedListener{

		private final DisruptorConsumer<AccumulatedEvent<T>> nextStageConsumer;
		
		public DisruptorEventConsumer(DisruptorConsumer<AccumulatedEvent<T>> nextStageConsumer){
			this.nextStageConsumer = nextStageConsumer;
		}
		
		@Override
		public void consume(AccumulatedEvent<T> event) {
			LOG.debug("Pushing event {} into disruptor queue", event);
			nextStageConsumer.consumeEvent(event);
		}

		@Override
		public void systemStopped() {
			nextStageConsumer.shutdown();
		}
	}
}
