package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemStartedListener;

/**
 * An event consumer that keeps track of the last time it pushed changes to the next
 * stage in the pipeline. Once an event is recieved after an alloted period of time after the
 * last event, the event consumer then triggers another event with the accumulated values
 * as paylaod
 * @author haines
 *
 */
public class PipelineAccumulatorEventConsumer<T extends Event> implements EventConsumer<T>, SystemStartedListener{

	private final AccumulatorEventConsumer<T> accumulatorConsumer;
	private final EventConsumer<AccumulatedEvent> nextStageConsumer;
	private final Clock systemClock;
	private long nextPushToPipe;
	private final PipelineAccumulatorConfig config;
	
	public PipelineAccumulatorEventConsumer(AccumulatorEventConsumer<T> accumulatorConsumer, EventConsumer<AccumulatedEvent> nextStageConsumer, Clock systemClock, PipelineAccumulatorConfig config){
		this.accumulatorConsumer = accumulatorConsumer;
		this.nextStageConsumer = nextStageConsumer;
		this.systemClock = systemClock;
		this.config = config;
	}

	@Override
	public void consume(T event) {
		accumulatorConsumer.consume(event);
		
		long currentTime = systemClock.getCurrentTime();
		
		if (currentTime > nextPushToPipe){ // we need to push data to pipe. Basically set up memory barrier for consumer to read data
			pushToPipe();
		}
	}

	private void pushToPipe() {
		
		AccumulatorProvider provider = accumulatorConsumer.getAccumulatorProvider();
		
		nextStageConsumer.consume(new AccumulatedEvent(provider));
		
		resetNextPipeTimeStamp();
		
	}

	@Override
	public void systemStarted() {
		resetNextPipeTimeStamp();
	}

	private void resetNextPipeTimeStamp() {
		nextPushToPipe = systemClock.getCurrentTime() + config.getPushIntervalTimeMs();
	}
	
	
}
