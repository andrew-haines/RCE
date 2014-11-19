package com.haines.ml.rce.main.factory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy.AccumulatorLookupStrategyFactory;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.DefaultRCEApplicationFactory.AccumulatorEventConsumerFactory;
import com.haines.ml.rce.main.factory.DefaultRCEApplicationFactory.DefaultASyncRCEApplicationFactory;
import com.haines.ml.rce.main.factory.DefaultRCEApplicationFactory.DefaultSyncRCEApplicationFactory;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes.VolatileNaiveBayesGlobalIndexesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesLocalIndexes;
import com.haines.ml.rce.window.WindowEventConsumer;
import com.haines.ml.rce.window.WindowManager;

public class NaiveBayesRCEApplicationFactory<E extends ClassifiedEvent, T extends AccumulatorLookupStrategy<? super E>> implements RCEApplicationFactory<E>{
	
	public static <E extends ClassifiedEvent> NaiveBayesRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy> getSyncNaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock){
		return getNaiveBayesRCEApplicationFactory(Mode.SYNC, marshalBuffer, config, manager, clock);
	}
	
	public static <E extends ClassifiedEvent> NaiveBayesRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy> getASyncNaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock){
		return getNaiveBayesRCEApplicationFactory(Mode.ASYNC, marshalBuffer, config, manager, clock);
	}
	
	private static <E extends ClassifiedEvent> NaiveBayesRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy> getNaiveBayesRCEApplicationFactory(Mode mode, EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock){
		
		EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy>> windowEventConsumer = new WindowEventConsumer(manager);
		final VolatileNaiveBayesGlobalIndexesProvider globalIndexes = new VolatileNaiveBayesGlobalIndexesProvider(new NaiveBayesGlobalIndexes());
		
		return new NaiveBayesRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy>(marshalBuffer, mode, config, windowEventConsumer, clock, new AccumulatorLookupStrategyFactory<E>() {

			@Override
			public AccumulatorLookupStrategy<? super E> create() {
				return new RONaiveBayesMapBasedLookupStrategy(new NaiveBayesLocalIndexes(globalIndexes));
			}
		});
	}
	
	
	private static enum Mode {
		ASYNC,
		SYNC
	}
	
	private final RCEApplicationFactory<E> defaultFactory;
	
	public NaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, Mode mode, RCEConfig config, EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy>> windowEventConsumer, Clock clock, AccumulatorLookupStrategyFactory<E> lookUpStrategy){
		
		AccumulatorEventConsumerFactory<E> factory = new AccumulatorEventConsumerFactory<E>(RCEConfig.UTIL.getAccumulatorConfig(config), lookUpStrategy);
		
		if (mode == Mode.ASYNC){
			defaultFactory = new DefaultASyncRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy>(marshalBuffer, factory, windowEventConsumer, getScheduledExecutor(), clock);
		} else if (mode == Mode.SYNC){
			defaultFactory = new DefaultSyncRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy>(marshalBuffer, factory, config, windowEventConsumer, clock);
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
}
