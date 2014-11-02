package com.haines.ml.rce.main.factory;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.haines.ml.rce.accumulator.AccumulatorEventConsumer;
import com.haines.ml.rce.accumulator.AsyncPipelineAccumulatorController;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.dispatcher.DispatcherConsumer;
import com.haines.ml.rce.dispatcher.DisruptorConfig;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor;
import com.haines.ml.rce.eventstream.SelectorEventStream;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventConsumerFactory;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.PipelinedEventConsumer;
import com.haines.ml.rce.model.system.Clock;

public class DefaultRCEApplicationFactory<E extends Event, EC extends EventConsumer<E>> implements RCEApplicationFactory{

	private final EventMarshalBuffer<E> marshalBuffer;
	private final EventConsumerFactory<E, EC> eventConsumerFactory;
	private final EventConsumer<AccumulatedEvent<?>> accumulatedEventConsumer;
	
	private DefaultRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, EventConsumerFactory<E, EC> eventConsumerFactory, EventConsumer<AccumulatedEvent<?>> accumulatedEventConsumer){
		this.marshalBuffer = marshalBuffer;
		this.eventConsumerFactory = eventConsumerFactory;
		this.accumulatedEventConsumer = accumulatedEventConsumer;
	}

	@Override
	public RCEApplication createApplication(String configOverrideLocation) {
		
		RCEConfig config;
		try {
			config = RCEApplicationFactory.UTIL.loadConfig(configOverrideLocation);
			RCEApplication application = new RCEApplication(getSelectorEventStream(config));
			
			return application;
		} catch (Exception e){
			throw new RuntimeException("Unable to instantiate RCE application", e);
		}
	}
	
	protected Clock getClock() {
		return Clock.SYSTEM_CLOCK;
	}

	private <T extends SelectableChannel & NetworkChannel> SelectorEventStream<T, E> getSelectorEventStream(RCEConfig config){
		
		Dispatcher<E> dispatcher = getDispatcher(config);
		
		@SuppressWarnings("unchecked")
		NetworkChannelProcessor<T> channelProcessor = (NetworkChannelProcessor<T>)RCEConfig.UTIL.getNetworkChannelProcessorProvider(config);
		
		EventMarshalBuffer<E> eventBuffer = this.marshalBuffer;
		EventStreamListener streamListener = EventStreamListener.UTIL.chainListeners(new EventStreamListener.SLF4JStreamListener());
		
		return new SelectorEventStream<T, E>(dispatcher, RCEConfig.UTIL.getSelectorEventStreamConfig(config), channelProcessor, eventBuffer, streamListener);
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
	protected Iterable<DispatcherConsumer<E>> getDispatcherConsumers(RCEConfig config, EventConsumerFactory<E, EC> consumerFactory, EventConsumer<AccumulatedEvent<?>> windowEventConsumer) {
		DisruptorConfig disruptorConfig = RCEConfig.UTIL.getDisruptorConfig(config);
		
		Iterable<EC> consumers = getEventConsumers(config, consumerFactory, windowEventConsumer);
		
		List<DispatcherConsumer<E>> workers = new ArrayList<DispatcherConsumer<E>>();
		
		for (EC consumer: consumers){ // create a new disptcher for each down stream consumer
			workers.add(new DisruptorConsumer.Builder<E>(Executors.newSingleThreadExecutor(), disruptorConfig)
						.addConsumer(consumer)
						.build());
		}
		
		return workers;
	}
	
	protected Iterable<EC> getEventConsumers(RCEConfig config, EventConsumerFactory<E, EC> factory, EventConsumer<AccumulatedEvent<?>> windowEventConsumer){
		List<EC> consumers = new ArrayList<EC>(config.getNumberOfEventWorkers());
		
		for (int i = 0; i < config.getNumberOfEventWorkers(); i++){
			consumers.add(factory.create());
		}
		
		return consumers;
	}
	
	public static class DefaultASyncRCEApplicationFactory<E extends Event> extends DefaultRCEApplicationFactory<E, PipelinedEventConsumer<E, AccumulatorEventConsumer<E>>>{
		
		private final ScheduledExecutorService executorService;
		public DefaultASyncRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, EventConsumerFactory<E, AccumulatorEventConsumer<E>> downstreamConsumerFactory, EventConsumer<AccumulatedEvent<?>> accumulatedEventConsumer, ScheduledExecutorService executorService){
			super(marshalBuffer, new SyncEventConsumerFactory<E, AccumulatorEventConsumer<E>>(downstreamConsumerFactory), accumulatedEventConsumer);
			
			this.executorService = executorService;
		}
		
		private static class SyncEventConsumerFactory<E extends Event, EC extends EventConsumer<E>> implements EventConsumerFactory<E, PipelinedEventConsumer<E,EC>>{

			private final EventConsumerFactory<E, EC> downstreamConsumerFactory;
			
			
			private SyncEventConsumerFactory(EventConsumerFactory<E, EC> downstreamConsumerFactory){
				this.downstreamConsumerFactory = downstreamConsumerFactory;
			}
			@Override
			public PipelinedEventConsumer<E, EC> create() {
				return new PipelinedEventConsumer<E, EC>(downstreamConsumerFactory.create(), downstreamConsumerFactory.create());
			}
		}

		@Override
		protected Iterable<PipelinedEventConsumer<E, AccumulatorEventConsumer<E>>> getEventConsumers(RCEConfig config, EventConsumerFactory<E, PipelinedEventConsumer<E, AccumulatorEventConsumer<E>>> consumerFactory, EventConsumer<AccumulatedEvent<?>> windowEventConsumer) {
			Iterable<PipelinedEventConsumer<E, AccumulatorEventConsumer<E>>> consumers = super.getEventConsumers(config, consumerFactory, windowEventConsumer);
			
			AsyncPipelineAccumulatorController<E> asyncController = new AsyncPipelineAccumulatorController<E>(getClock(), RCEConfig.UTIL.getPipelineAccumulatorConfig(config), consumers, windowEventConsumer, executorService);
			
			return consumers;
		}
	}
}
