package com.haines.ml.rce.main.factory;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy.AccumulatorLookupStrategyFactory;
import com.haines.ml.rce.accumulator.AsyncPipelineAccumulatorController;
import com.haines.ml.rce.accumulator.PipelineAccumulatorController.PipelineAccumulatorControllerFactory;
import com.haines.ml.rce.accumulator.SyncPipelineEventConsumer;
import com.haines.ml.rce.accumulator.handlers.ClassifiedEventAccumulatorConsumer;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.dispatcher.DispatcherConsumer;
import com.haines.ml.rce.dispatcher.DisruptorConfig;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor;
import com.haines.ml.rce.eventstream.SelectorEventStream;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor.NetworkChannelProcessorProvider;
import com.haines.ml.rce.eventstream.SelectorEventStreamFactory;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.RCEApplication.DefaultRCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventConsumerFactory;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.FeaturedEvent;
import com.haines.ml.rce.model.PipelinedEventConsumer;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.model.system.SystemStartedListener;

public class DefaultRCEApplicationFactory<E extends Event, EC extends EventConsumer<E>, T extends AccumulatorLookupStrategy<? super E>> implements RCEApplicationFactory<E>{

	private static final ThreadFactory SELECTOR_THREAD_FACTORY = new ThreadFactory(){

		private int threadNum = 0;
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "DisruptorSelectorThread_"+threadNum++);
		}
	};
	
	private final EventMarshalBuffer<E> marshalBuffer;
	private final EventConsumerFactory<E, EC> eventConsumerFactory;
	private final EventConsumer<AccumulatedEvent<T>> accumulatedEventConsumer;
	private final Collection<SystemListener> systemListeners;
	private RCEConfig overrideConfig;
	private FeatureHandlerRepositoryFactory featureHandlerRepo;
	private final Clock clock;
	
	private DefaultRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, Clock clock, EventConsumerFactory<E, EC> eventConsumerFactory, EventConsumer<AccumulatedEvent<T>> accumulatedEventConsumer){
		this.marshalBuffer = marshalBuffer;
		this.eventConsumerFactory = eventConsumerFactory;
		this.accumulatedEventConsumer = accumulatedEventConsumer;
		
		this.systemListeners = new ArrayList<SystemListener>();
		
		if (eventConsumerFactory instanceof SystemStartedListener){
			systemListeners.add((SystemStartedListener)eventConsumerFactory);
		}
		
		this.clock = clock;
	}

	@Override
	public RCEApplication<E> createApplication(String configOverrideLocation) {
		
		RCEConfig config = overrideConfig;
		try {
			if (config == null){
				config = RCEApplicationFactory.UTIL.loadConfig(configOverrideLocation);
			}
			SelectorEventStream<?, E> eventStream = getSelectorEventStream(config, clock);
			RCEApplication<E> application = new DefaultRCEApplication<E>(eventStream, eventStream, config, systemListeners);
			
			return application;
		} catch (Exception e){
			throw new RuntimeException("Unable to instantiate RCE application", e);
		}
	}
	
	@Override
	public void addSystemListeners(Iterable<? extends SystemListener> startupListeners) {
		for (SystemListener listener: startupListeners){
			this.systemListeners.add(listener);
		}
	}
	
	protected void addSystemListener(SystemListener listener){
		this.systemListeners.add(listener);
	}
	
	protected Clock getClock() {
		return clock;
	}

	private <S extends SelectableChannel & NetworkChannel> SelectorEventStream<S, E> getSelectorEventStream(RCEConfig config, Clock clock){
		
		Dispatcher<E> dispatcher = getDispatcher(config);
		
		NetworkChannelProcessorProvider<?> channelProcessorProvider = RCEConfig.UTIL.getNetworkChannelProcessorProvider(config);
		
		@SuppressWarnings("unchecked")
		NetworkChannelProcessor<S> channelProcessor = (NetworkChannelProcessor<S>)channelProcessorProvider.get();
		
		EventMarshalBuffer<E> eventBuffer = this.marshalBuffer;
		EventStreamListener streamListener = EventStreamListener.UTIL.chainListeners(Iterables.filter(systemListeners, EventStreamListener.class));
		
		SelectorEventStreamFactory<S, E> factory = new SelectorEventStreamFactory<S, E>(clock, RCEConfig.UTIL.getSelectorEventStreamConfig(config), channelProcessor, eventBuffer, streamListener);
		
		return factory.create(dispatcher);
	}

	private Dispatcher<E> getDispatcher(RCEConfig config) {
		
		Iterable<DispatcherConsumer<E>> consumers = getDispatcherConsumers(config, eventConsumerFactory, accumulatedEventConsumer);
		
		return new Dispatcher<E>(consumers);
	}

	/**
	 * Default to a disruptor queued dispatchers. Override to define other queue implementations if required.
	 * @param config
	 * @param consumerFactory
	 * @return
	 */
	protected Iterable<DispatcherConsumer<E>> getDispatcherConsumers(RCEConfig config, EventConsumerFactory<E, EC> consumerFactory, EventConsumer<AccumulatedEvent<T>> windowEventConsumer) {
		DisruptorConfig disruptorConfig = RCEConfig.UTIL.getDisruptorConfig(config);
		
		Iterable<EC> consumers = getEventConsumers(config, consumerFactory, windowEventConsumer);
		
		List<DispatcherConsumer<E>> workers = new ArrayList<DispatcherConsumer<E>>();
		
		for (EC consumer: consumers){ // create a new disptcher for each down stream consumer
			workers.add(new DisruptorConsumer.Builder<E>(Executors.newSingleThreadExecutor(SELECTOR_THREAD_FACTORY), disruptorConfig)
						.addConsumer(consumer)
						.build());
		}
		
		return workers;
	}
	
	protected Iterable<EC> getEventConsumers(RCEConfig config, EventConsumerFactory<E, EC> factory, EventConsumer<AccumulatedEvent<T>> windowEventConsumer){
		List<EC> consumers = new ArrayList<EC>(config.getNumberOfEventWorkers());
		
		for (int i = 0; i < config.getNumberOfEventWorkers(); i++){
			consumers.add(factory.create());
		}
		
		return consumers;
	}
		
	public static class FeaturedAccumulatorEventConsumerFactory<E extends ClassifiedEvent> implements EventConsumerFactory<E, Accumulator<E>>{

		private final AccumulatorConfig config;
		private final AccumulatorLookupStrategyFactory<E> lookupStrategyFactory;
		private final HandlerRepository<E> featureHandlerRepo;
		
		@Inject
		public FeaturedAccumulatorEventConsumerFactory(AccumulatorConfig config, AccumulatorLookupStrategyFactory<E> lookupStrategyFactory, HandlerRepository<E> featureHandlerRepo){
			this.config = config;
			this.lookupStrategyFactory = lookupStrategyFactory;
			this.featureHandlerRepo = featureHandlerRepo;
		}
		
		@Override
		public Accumulator<E> create() {
			AccumulatorLookupStrategy<? super E> lookupStrategy = lookupStrategyFactory.create();
			
			return new ClassifiedEventAccumulatorConsumer<E>(config, lookupStrategy, featureHandlerRepo);
		}
	}
	
	public static class DefaultASyncRCEApplicationFactory<E extends FeaturedEvent, T extends AccumulatorLookupStrategy<? super E>> extends DefaultRCEApplicationFactory<E, PipelinedEventConsumer<E, Accumulator<E>>, T>{
		
		private final ScheduledExecutorService executorService;
		public DefaultASyncRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, EventConsumerFactory<E, Accumulator<E>> downstreamConsumerFactory, EventConsumer<AccumulatedEvent<T>> accumulatedEventConsumer, ScheduledExecutorService executorService, Clock clock){
			super(marshalBuffer, clock, new ASyncEventConsumerFactory<E, Accumulator<E>>(downstreamConsumerFactory), accumulatedEventConsumer);
			
			this.executorService = executorService;
		}
		
		private static class ASyncEventConsumerFactory<E extends Event, EC extends EventConsumer<E>> implements EventConsumerFactory<E, PipelinedEventConsumer<E,EC>>{

			private final EventConsumerFactory<E, EC> downstreamConsumerFactory;
			
			
			private ASyncEventConsumerFactory(EventConsumerFactory<E, EC> downstreamConsumerFactory){
				this.downstreamConsumerFactory = downstreamConsumerFactory;
			}
			@Override
			public PipelinedEventConsumer<E, EC> create() {
				return new PipelinedEventConsumer<E, EC>(downstreamConsumerFactory.create(), downstreamConsumerFactory.create());
			}
		}

		@Override
		protected Iterable<PipelinedEventConsumer<E, Accumulator<E>>> getEventConsumers(RCEConfig config, EventConsumerFactory<E, PipelinedEventConsumer<E, Accumulator<E>>> consumerFactory, EventConsumer<AccumulatedEvent<T>> windowEventConsumer) {
			Iterable<PipelinedEventConsumer<E, Accumulator<E>>> consumers = super.getEventConsumers(config, consumerFactory, windowEventConsumer);
			
			AsyncPipelineAccumulatorController<E, T> asyncController = new AsyncPipelineAccumulatorController<E, T>(getClock(), RCEConfig.UTIL.getPipelineAccumulatorConfig(config), consumers, windowEventConsumer, executorService);
			
			super.addSystemListener(asyncController);
			return consumers;
		}
	}
	
	public static class DefaultSyncRCEApplicationFactory<E extends Event, T extends AccumulatorLookupStrategy<? super E>> extends DefaultRCEApplicationFactory<E, SyncPipelineEventConsumer<E, T>, T>{
		
		public DefaultSyncRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, EventConsumerFactory<E, Accumulator<E>> factory, RCEConfig config, EventConsumer<AccumulatedEvent<T>> windowEventConsumer, Clock clock){
			super(marshalBuffer, clock, DefaultSyncRCEApplicationFactory.<E, T>getSynEventConsumerFactory(clock, config, factory, windowEventConsumer), windowEventConsumer);
		}
		
		private static <E extends Event, T extends AccumulatorLookupStrategy<? super E>> EventConsumerFactory<E, SyncPipelineEventConsumer<E, T>> getSynEventConsumerFactory(Clock clock, RCEConfig config, EventConsumerFactory<E, Accumulator<E>> factory, EventConsumer<AccumulatedEvent<T>> windowEventConsumer) {
			
			return new SyncEventConsumerFactory<E, T>(new PipelineAccumulatorControllerFactory(clock, RCEConfig.UTIL.getPipelineAccumulatorConfig(config)), factory, windowEventConsumer);
		}
		
		private static class SyncEventConsumerFactory<E extends Event, T extends AccumulatorLookupStrategy<? super E>> implements EventConsumerFactory<E, SyncPipelineEventConsumer<E, T>>, SystemStartedListener {

			private final PipelineAccumulatorControllerFactory controller;
			private final EventConsumerFactory<E, Accumulator<E>> eventConsumerFactory;
			private final EventConsumer<AccumulatedEvent<T>> accumulatedEventConsumer;
			
			private SyncEventConsumerFactory(PipelineAccumulatorControllerFactory controller, EventConsumerFactory<E, Accumulator<E>> eventConsumerFactory, EventConsumer<AccumulatedEvent<T>> accumulatedEventConsumer){
				this.controller = controller;
				this.accumulatedEventConsumer = accumulatedEventConsumer;
				this.eventConsumerFactory = eventConsumerFactory;
			}
			
			@Override
			public SyncPipelineEventConsumer<E, T> create() {
				return new SyncPipelineEventConsumer<E, T>(controller.create(), eventConsumerFactory.create(), accumulatedEventConsumer);
			}

			@Override
			public void systemStarted() {
			}
			
		}
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		this.overrideConfig = config;
	}

	@Override
	public void useSpecificHandlerRepository(FeatureHandlerRepositoryFactory featureHandlerRepo) {
		this.featureHandlerRepo = featureHandlerRepo;
	}
	
	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepo() {
		return featureHandlerRepo;
	}
}
