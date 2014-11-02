package com.haines.ml.rce.accumulator;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.PipelinedEventConsumer;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemStartedListener;

public class AsyncPipelineAccumulatorController<E extends Event> extends PipelineAccumulatorController implements Runnable, SystemStartedListener{

	public static final String SCHEDULE_EXECUTOR_BINDING_KEY = "AsyncPipelineAccumulatorController.asyncExecutor";
	private final Logger LOG = LoggerFactory.getLogger(AsyncPipelineAccumulatorController.class);
	private final Iterable<PipelinedEventConsumer<E, AccumulatorEventConsumer<E>>> consumers;
	private final EventConsumer<AccumulatedEvent<?>> accumulatorConsumer;
	private final ScheduledExecutorService executorService;
	private volatile boolean isRunning;
	
	@Inject
	public AsyncPipelineAccumulatorController(Clock systemClock, PipelineAccumulatorConfig config, Iterable<PipelinedEventConsumer<E, AccumulatorEventConsumer<E>>> consumers, EventConsumer<AccumulatedEvent<?>> accumulatorConsumer, @Named(SCHEDULE_EXECUTOR_BINDING_KEY) ScheduledExecutorService executorService) {
		super(systemClock, config);
		
		this.consumers = consumers;
		this.accumulatorConsumer = accumulatorConsumer;
		this.executorService = executorService;
	}

	@Override
	public void run() {
		
		LOG.debug("Async Pipeline controller Started");
		
		process();
	}
	
	private void process(){
		
		for (PipelinedEventConsumer<E, AccumulatorEventConsumer<E>> consumer: consumers){
			AccumulatorEventConsumer<E> stagingConsumer = consumer.switchLiveConsumer();
			
			super.pushToPipe(stagingConsumer, accumulatorConsumer);
		}
	}

	@Override
	public void systemStarted() {
		super.systemStarted();
		
		executorService.scheduleAtFixedRate(this, config.getPushIntervalTimeMs(), config.getPushIntervalTimeMs(), TimeUnit.MILLISECONDS);
	}

}
