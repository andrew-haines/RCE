package com.haines.ml.rce.main.factory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy.AccumulatorLookupStrategyFactory;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.SyncPipelineEventConsumer;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.DefaultRCEApplicationFactory.FeaturedAccumulatorEventConsumerFactory;
import com.haines.ml.rce.main.factory.DefaultRCEApplicationFactory.DefaultASyncRCEApplicationFactory;
import com.haines.ml.rce.main.factory.DefaultRCEApplicationFactory.DefaultSyncRCEApplicationFactory;

public class AccumulatorRCEApplicationFactory<E extends ClassifiedEvent, T extends AccumulatorLookupStrategy<? super E>> implements RCEApplicationFactory<E>{
	
	private static final ThreadFactory ACCUMULATED_EVENT_THREAD_FACTORY = new ThreadFactory(){

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "DisruptorAccumulatorThread");
		}
	};
	
	public static enum Mode {
		ASYNC,
		SYNC
	}
	
	private final RCEApplicationFactory<E> defaultFactory;
	
	public AccumulatorRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, Mode mode, RCEConfig config, EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>>> windowEventConsumer, Clock clock, AccumulatorLookupStrategyFactory<E> lookUpStrategy, HandlerRepository<E> featureHandlerRepo){
		
		FeaturedAccumulatorEventConsumerFactory<E> factory = new FeaturedAccumulatorEventConsumerFactory<E>(RCEConfig.UTIL.getAccumulatorConfig(config), lookUpStrategy, featureHandlerRepo);
		
		if (mode == Mode.ASYNC){
			defaultFactory = new DefaultASyncRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy<E>>(marshalBuffer, factory, windowEventConsumer, getScheduledExecutor(), clock);
		} else if (mode == Mode.SYNC){
			
			windowEventConsumer = new SyncPipelineEventConsumer.DisruptorEventConsumer<E, RONaiveBayesMapBasedLookupStrategy<E>>(new DisruptorConsumer.Builder<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>>>(Executors.newSingleThreadExecutor(ACCUMULATED_EVENT_THREAD_FACTORY), RCEConfig.UTIL.getDisruptorConfig(config))
					.addConsumer(windowEventConsumer)
					.build());
			
			defaultFactory = new DefaultSyncRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy<E>>(marshalBuffer, factory, config, windowEventConsumer, clock);
		} else{
			throw new IllegalArgumentException("Unknown mode type: "+mode);
		}
	}

	private ScheduledExecutorService getScheduledExecutor() {
		return Executors.newSingleThreadScheduledExecutor(new ThreadFactory(){

			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "AsyncExecutorService");
			}
			
		});
	}
	
	@Override
	public void addSystemListeners(Iterable<SystemListener> startupListeners) {
		this.defaultFactory.addSystemListeners(startupListeners);
	}

	@Override
	public RCEApplication<E> createApplication(String configOverrideLocation) {
		return defaultFactory.createApplication(configOverrideLocation);
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		this.defaultFactory.useSpecificConfig(config);
	}

	@Override
	public void useSpecificHandlerRepository(FeatureHandlerRepositoryFactory featureHandlerRepo) {
		this.defaultFactory.useSpecificHandlerRepository(featureHandlerRepo);
		
	}
}
