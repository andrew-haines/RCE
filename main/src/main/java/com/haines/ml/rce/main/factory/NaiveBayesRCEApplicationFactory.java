package com.haines.ml.rce.main.factory;

import java.io.IOException;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBException;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.HandlerRepository;
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
import com.haines.ml.rce.window.WindowConfig;
import com.haines.ml.rce.window.WindowEventConsumer;
import com.haines.ml.rce.window.WindowManager;
import com.haines.ml.rce.window.WindowUpdatedListener;

public class NaiveBayesRCEApplicationFactory<E extends ClassifiedEvent> implements RCEApplicationFactory<E>{

	public static <E extends ClassifiedEvent> AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy<E>> getSyncNaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock, HandlerRepository<E> featureHandlerRepo, WindowConfig windowConfig){
		return getNaiveBayesRCEApplicationFactory(Mode.SYNC, marshalBuffer, config, manager, clock, featureHandlerRepo, windowConfig);
	}
	
	public static <E extends ClassifiedEvent> AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy<E>> getASyncNaiveBayesRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock, HandlerRepository<E> featureHandlerRepo, WindowConfig windowConfig){
		return getNaiveBayesRCEApplicationFactory(Mode.ASYNC, marshalBuffer, config, manager, clock, featureHandlerRepo, windowConfig);
	}
	
	private static <E extends ClassifiedEvent> AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy<E>> getNaiveBayesRCEApplicationFactory(Mode mode, EventMarshalBuffer<E> marshalBuffer, RCEConfig config, WindowManager manager, Clock clock, HandlerRepository<E> featureHandlerRepo, WindowConfig windowConfig){
		
		final VolatileNaiveBayesGlobalIndexesProvider globalIndexes = new VolatileNaiveBayesGlobalIndexesProvider(new NaiveBayesGlobalIndexes());
		
		EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>>> windowEventConsumer = new WindowEventConsumer<E>(manager, featureHandlerRepo, windowConfig, globalIndexes);
		
		return new AccumulatorRCEApplicationFactory<E, RONaiveBayesMapBasedLookupStrategy<E>>(marshalBuffer, mode, config, windowEventConsumer, clock, new AccumulatorLookupStrategyFactory<E>() {

			@Override
			public AccumulatorLookupStrategy<? super E> create() {
				return new RONaiveBayesMapBasedLookupStrategy<E>(new NaiveBayesLocalIndexes(globalIndexes));
			}
		}, featureHandlerRepo);
	}
	
	private final EventMarshalBuffer<E> marshalBuffer;
	private final Mode mode;
	private Iterable<? extends SystemListener> startupListeners = null;
	private RCEConfig config;
	private FeatureHandlerRepositoryFactory featureHandlerRepo = null;
	
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
			
			if (featureHandlerRepo == null){ // if we haven't had a hander explicitly set then use the service loader to find one
				for (FeatureHandlerRepositoryFactory factory: ServiceLoader.load(FeatureHandlerRepositoryFactory.class)){
					featureHandlerRepo = factory;
				}
				
				if (featureHandlerRepo == null){ // if we still do not have a feature hnadler set then use the universally discrete one
					featureHandlerRepo = FeatureHandlerRepositoryFactory.ALL_DISCRETE_FEATURES;
				}
			}
			
			HandlerRepository<E> repo = featureHandlerRepo.create();
			
			WindowConfig windowConfig = RCEConfig.UTIL.getWindowConfig(config);
			
			WindowManager manager = new WindowManager(windowConfig, clock, Iterables.filter(startupListeners, WindowUpdatedListener.class), repo);
			
			NaiveBayesService classifierService = new NaiveBayesService(manager);
			
			RCEApplicationFactory<E> factory = NaiveBayesRCEApplicationFactory.getNaiveBayesRCEApplicationFactory(mode, marshalBuffer, config, manager, clock, repo, windowConfig);
			
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
	public void addSystemListeners(Iterable<? extends SystemListener> startupListeners) {
		this.startupListeners = startupListeners;
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		this.config = config;
	}
	
	public void useSpecificHandlerRepository(FeatureHandlerRepositoryFactory featureHandlerRepo){
		this.featureHandlerRepo = featureHandlerRepo;
	}
}
