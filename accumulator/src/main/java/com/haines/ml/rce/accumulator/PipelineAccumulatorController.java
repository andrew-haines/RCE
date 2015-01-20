package com.haines.ml.rce.accumulator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.PipelinedEventConsumer;
import com.haines.ml.rce.model.system.Clock;

/**
 * An event consumer that keeps track of the last time it pushed changes to the next
 * stage in the pipeline. Once an event is received after an alloted period of time after the
 * last event, the event consumer then triggers another event with the accumulated values
 * as paylaod. This can run in 2 modes:
 * 
 * Synchronous - Here, as events are processed as they are received, the server time is checked to see
 *               if the next pipeline push is required. If so, the accumulator is queried, the state extracted
 *               and passed on to the consumer at the next stage of the pipeline. Advantage to this approach
 *               is that the writer thread has complete control and ownership of this consumer so no volatile or
 *               live/staging access is required. The downside is that events have to be seen to push previous events
 *               to downstream consumers. Consequently, if no events are recieved, nothing can push previously seen events
 *               downstream. Also downstream pushes from different instances of this class will happen at different times, possibly
 *               causing downstream state to be none-representative at certain periods. This shouldnt be a problem as state can either
 *               wait until all upstream consumers have pushed or we just accept that the probability distribution will be inconsistant
 *               at certain periods as the events are all pushed downstream. If the system is under high degrees of load then this
 *               period will be very small and this mode maybe the most appropriate and efficient. Another small downside to this
 *               mode is that, on pushing to the downstream pipe, the thread will obviously be performing more work and therefore,
 *               throughput during this process will be stopped.
 *               
 * ASynchronous - Here we have a background task that wakes up periodically and performs the push downstream of all accumulator
 * 				  consumers. This has the draw back that writer ownership needs to be transfered to the background task which involves
 * 				  the use of a {@link PipelinedEventConsumer} to wrap the consumer so that 2 environments are used, staging and live.
 * 				  This requires double the memory footprint and a volatile read/write. This mode will enable events to be processed
 * 				  pretty much atonomously (there will still be a period when switching consumers between environments when changing
 * 				  ownership where events will be slightly inconsistent although its overall effect on the probability distribution will
 * 				  be negligable). Also, if the system is not under large load and the potential accumulator size small, then this is the
 * 				  better mode of operation.
 * 
 * @author haines
 *
 */
public class PipelineAccumulatorController {

	private static final Logger LOG = LoggerFactory.getLogger(PipelineAccumulatorController.class);
	
	private final Clock clock;
	private long nextPushToPipe;
	protected final PipelineAccumulatorConfig config;
	
	@Inject
	public PipelineAccumulatorController(Clock clock, PipelineAccumulatorConfig config){
		this.clock = clock;
		this.config = config;
	}
	
	<E extends Event, T extends AccumulatorLookupStrategy<? super E>> void pushIfRequired(Accumulator<E> sourceConsumer, EventConsumer<AccumulatedEvent<T>> nextStageConsumer){
		long currentTime = clock.getCurrentTime();
		
		if (currentTime >= nextPushToPipe){ // we need to push data to pipe. Basically set up memory barrier for consumer to read data
			pushToPipe(sourceConsumer, nextStageConsumer);
		} else{
			//LOG.debug("Skipping push as {} is not greater than push time {}", currentTime, nextPushToPipe);
		}
	}

	protected <E extends Event, T extends AccumulatorLookupStrategy<?>> void pushToPipe(Accumulator<E> sourceConsumer, EventConsumer<AccumulatedEvent<T>> nextStageConsumer) {
		LOG.debug("Pushing to downstream consumer at push time: "+nextPushToPipe);

		AccumulatorProvider<E> provider = sourceConsumer.getAccumulatorProvider(); // we now control this accumulator. All operations are now atomic.

		@SuppressWarnings("unchecked")
		T lookupStrategy = (T)provider.getLookupStrategy();
		
		sourceConsumer.clear();
		nextStageConsumer.consume(new AccumulatedEvent<T>(provider, lookupStrategy));
		
		resetNextPipeTimeStamp();
		LOG.debug("downstream consumer push completed. Next push time at: "+nextPushToPipe);
	}

	private void resetNextPipeTimeStamp() {
		nextPushToPipe = clock.getCurrentTime() + config.getPushIntervalTimeMs();
	}
	
	public static class PipelineAccumulatorControllerFactory {
		
		private final Clock clock;
		private final PipelineAccumulatorConfig config;
		
		public PipelineAccumulatorControllerFactory(Clock clock, PipelineAccumulatorConfig config){
			this.clock = clock;
			this.config = config;
		}
		
		public PipelineAccumulatorController create(){
			return new PipelineAccumulatorController(clock, config);
		}
	}
}
