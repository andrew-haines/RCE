package com.haines.ml.rce.main.factory;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy.AccumulatorLookupStrategyFactory;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesLocalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes.VolatileNaiveBayesGlobalIndexesProvider;
import com.haines.ml.rce.window.WindowEventConsumer;
import com.haines.ml.rce.window.WindowManager;
import com.haines.ml.rce.window.WindowUpdatedListener;

public class NaiveBayesRCEApplicationFactory<E extends ClassifiedEvent> implements RCEApplicationFactory<E>{

	public static <E extends ClassifiedEvent> AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy> getSyncNaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock){
		return getNaiveBayesRCEApplicationFactory(Mode.SYNC, marshalBuffer, config, manager, clock);
	}
	
	public static <E extends ClassifiedEvent> AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy> getASyncNaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock){
		return getNaiveBayesRCEApplicationFactory(Mode.ASYNC, marshalBuffer, config, manager, clock);
	}
	
	private static <E extends ClassifiedEvent> AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy> getNaiveBayesRCEApplicationFactory(Mode mode, EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock){
		
		EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy>> windowEventConsumer = new WindowEventConsumer(manager);
		final VolatileNaiveBayesGlobalIndexesProvider globalIndexes = new VolatileNaiveBayesGlobalIndexesProvider(new NaiveBayesGlobalIndexes());
		
		return new AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy>(marshalBuffer, mode, config, windowEventConsumer, clock, new AccumulatorLookupStrategyFactory<E>() {

			@Override
			public AccumulatorLookupStrategy<? super E> create() {
				return new RONaiveBayesMapBasedLookupStrategy(new NaiveBayesLocalIndexes(globalIndexes));
			}
		});
	}
	
	private final EventMarshalBuffer<E> marshalBuffer;
	private final Mode mode;
	private Iterable<SystemListener> startupListeners = null;
	private RCEConfig config;
	
	public NaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, Mode mode){
		this.marshalBuffer = marshalBuffer;
		this.mode = mode;
	}
	
	@Override
	public NaiveBayesRCEApplication<E> createApplication(String configOverrideLocation) {
		
		try {
			
			RCEConfig config = this.config;
			
			if (config == null){
				config = RCEApplicationFactory.UTIL.loadConfig(configOverrideLocation);
			}
			Clock clock = Clock.SYSTEM_CLOCK;
			
			WindowManager manager = new WindowManager(RCEConfig.UTIL.getWindowConfig(config), clock, Iterables.filter(startupListeners, WindowUpdatedListener.class));
			
			NaiveBayesService classifierService = new NaiveBayesService(manager);
			
			RCEApplicationFactory<E> factory = NaiveBayesRCEApplicationFactory.getNaiveBayesRCEApplicationFactory(mode, marshalBuffer, config, manager, clock);
			
			if (startupListeners != null){
				factory.addSystemListeners(startupListeners);
			}
			
			factory.useSpecificConfig(config);
			
			return new NaiveBayesRCEApplication<E>(factory.createApplication(configOverrideLocation), classifierService);
		} catch (JAXBException | IOException e) {
			throw new RuntimeException("Unable to create test naive bayes RCE application factory", e);
		}
	}

	@Override
	public void addSystemListeners(Iterable<SystemListener> startupListeners) {
		this.startupListeners = startupListeners;
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		this.config = config;
	}
}
