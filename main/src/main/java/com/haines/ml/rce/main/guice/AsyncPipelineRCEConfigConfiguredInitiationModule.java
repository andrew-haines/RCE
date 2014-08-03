package com.haines.ml.rce.main.guice;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.name.Names;
import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.AccumulatorEventConsumer;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AsyncPipelineAccumulatorController;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventConsumerFactory;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.PipelinedEventConsumer;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes.VolatileNaiveBayesGlobalIndexesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesLocalIndexes;

public class AsyncPipelineRCEConfigConfiguredInitiationModule<T extends SelectableChannel & NetworkChannel, E extends Event> extends RCEConfigConfiguredInitiationModule<T, E>{
	
	private static final String DOWNSTREAM_CONSUMER_BIND_KEY = "downstreamConsumer";

	public AsyncPipelineRCEConfigConfiguredInitiationModule(String overrideLocation, Class<E> eventType, Class<T> networkChannelType, Provider<EventMarshalBuffer<E>> eventMarshaller) {
		super(overrideLocation, eventType, networkChannelType, eventMarshaller);
	}
	
	@Override
	protected void configure() {
		super.configure();
		
//		bind(new TypeLiteral<EventConsumerFactory<E, ? extends EventConsumer<E>>>(){}).annotatedWith(Names.named(DOWNSTREAM_CONSUMER_BIND_KEY)).to(getDownStreamEventConsumerFactory()).in(Scopes.NO_SCOPE);
//		bind(AccumulatorConfig.class).toProvider(AccumulatorConfigProvider.class).in(Scopes.SINGLETON);
//		bind(AccumulatorLookupStrategy.class).to(RONaiveBayesMapBasedLookupStrategy.class).in(Scopes.NO_SCOPE); // a new instance every time it is injected
//		bindIndexes(bind(NaiveBayesIndexes.class).annotatedWith(Names.named(RONaiveBayesMapBasedLookupStrategy.LOOKUP_STRATEGY_INDEXES)));
//		bind(AsyncPipelineAccumulatorController.class).in(Scopes.SINGLETON);
//		bind(ScheduledExecutorService.class).annotatedWith(Names.named(AsyncPipelineAccumulatorController.SCHEDULE_EXECUTOR_BINDING_KEY)).toInstance(Executors.newScheduledThreadPool(1, new ThreadFactory() {
//			
//			@Override
//			public Thread newThread(Runnable r) {
//				
//				return new Thread(r, "Async Pipeline Controller");
//			}
//		}));
	}

	protected void bindIndexes(LinkedBindingBuilder<NaiveBayesIndexes> builder) {
		builder.to(NaiveBayesLocalIndexes.class).in(Scopes.NO_SCOPE); // new instance for each cpu
		bind(NaiveBayesIndexesProvider.class).annotatedWith(Names.named(NaiveBayesLocalIndexes.INJECT_BINDING_GLOBAL_INDEXES_KEY)).to(VolatileNaiveBayesGlobalIndexesProvider.class).in(Scopes.SINGLETON); // single shared index set for all accumulators
		bind(NaiveBayesGlobalIndexes.class).in(Scopes.SINGLETON); // Note that this instance will become updated by the app during its lifecycle. This is just the initial guice bindings.
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Class<? extends EventConsumerFactory<E, ? extends EventConsumer<E>>> getDownStreamEventConsumerFactory() {
		return (Class)AccumulatorEventConsumerFactory.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Class<? extends EventConsumerFactory<E, ? extends EventConsumer<E>>> getEventConsumerFactoryType() {
		return (Class)PipelineEventConsumerFactory.class;
	}
	
	public static class PipelineEventConsumerFactory<E extends Event, EC extends EventConsumer<E>> implements EventConsumerFactory<E, PipelinedEventConsumer<E, EC>>{

		private final EventConsumerFactory<E, EC> downstreamConsumer;
		
		@Inject
		public PipelineEventConsumerFactory(@Named(DOWNSTREAM_CONSUMER_BIND_KEY) EventConsumerFactory<E, EC> downstreamConsumer){
			this.downstreamConsumer = downstreamConsumer;
		}

		@Override
		public PipelinedEventConsumer<E, EC> create() {
			return new PipelinedEventConsumer<E, EC>(downstreamConsumer.create(), downstreamConsumer.create());
		}
	}
	
	public static class AccumulatorEventConsumerFactory<E extends Event> implements EventConsumerFactory<E, AccumulatorEventConsumer<E>>{

		private final AccumulatorConfig config;
		private final AccumulatorLookupStrategy<E> lookupStrategy;
		
		@Inject
		public AccumulatorEventConsumerFactory(AccumulatorConfig config, AccumulatorLookupStrategy<E> lookupStrategy){
			this.config = config;
			this.lookupStrategy = lookupStrategy;
		}
		
		@Override
		public AccumulatorEventConsumer<E> create() {
			return new AccumulatorEventConsumer<E>(config, lookupStrategy);
		}
	}
	
	public static class AccumulatorConfigProvider implements Provider<AccumulatorConfig>{

		private final RCEConfig config;
		
		@Inject
		public AccumulatorConfigProvider(RCEConfig config){
			this.config = config;
		}
		
		@Override
		public AccumulatorConfig get() {
			return new AccumulatorConfig(){

				@Override
				public int getFirstAccumulatorLineBitDepth() {
					return config.getFirstAccumulatorLineBitDepth();
				}

				@Override
				public int getSecondAccumulatorLineBitDepth() {
					return config.getSecondAccumulatorLineBitDepth();
				}

				@Override
				public int getFinalAccumulatorLineBitDepth() {
					return config.getFinalAccumulatorLineBitDepth();
				}
				
			};
		}
		
	}
}
