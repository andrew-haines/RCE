package com.haines.ml.rce.main.factory;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import com.haines.ml.rce.accumulator.AsyncPipelineAccumulatorController;
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

public class DefaultRCEApplicationFactory<E extends Event> implements RCEApplicationFactory{

	private final EventMarshalBuffer<E> marshalBuffer;
	private final EventConsumerFactory<E, ? extends EventConsumer<E>> eventConsumerFactory;
	
	private DefaultRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, EventConsumerFactory<E, ? extends EventConsumer<E>> eventConsumerFactory){
		this.marshalBuffer = marshalBuffer;
		this.eventConsumerFactory = eventConsumerFactory;
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
		
		Iterable<DispatcherConsumer<E>> consumers = getDispatcherConsumers(config, eventConsumerFactory);
		
		return new Dispatcher<E>(consumers);
	}

	/**
	 * Default to a disruptor queued dispatchers. Override to define other queue implementations if required.
	 * @param config
	 * @param consumerFactory
	 * @return
	 */
	protected <EC extends EventConsumer<E>> Iterable<DispatcherConsumer<E>> getDispatcherConsumers(RCEConfig config, EventConsumerFactory<E, EC> consumerFactory) {
		DisruptorConfig disruptorConfig = RCEConfig.UTIL.getDisruptorConfig(config);
		
		List<DispatcherConsumer<E>> workers = new ArrayList<DispatcherConsumer<E>>(config.getNumberOfEventWorkers());
		
		for (int i = 0; i < config.getNumberOfEventWorkers(); i++){
			workers.add(new DisruptorConsumer.Builder<E>(Executors.newSingleThreadExecutor(), disruptorConfig)
						.addConsumer(consumerFactory.create())
						.build());
		}
		
		return workers;
	}
	
	public static class DefaultASyncRCEApplicationFactory<E extends Event> extends DefaultRCEApplicationFactory<E>{
		
		public DefaultASyncRCEApplicationFactory(EventMarshalBuffer<E> marshalBuffer, EventConsumerFactory<E, EventConsumer<E>> downstreamConsumerFactory){
			super(marshalBuffer, new SyncEventConsumerFactory<E, EventConsumer<E>>(downstreamConsumerFactory));
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
		protected <EC extends EventConsumer<E>> Iterable<DispatcherConsumer<E>> getDispatcherConsumers(
				RCEConfig config, EventConsumerFactory<E, EC> consumerFactory) {
			Iterable<DispatcherConsumer<E>> consumers = super.getDispatcherConsumers(config, consumerFactory);
			
			AsyncPipelineAccumulatorController<E, A> asyncController = new AsyncPipelineAccumulatorController<>(getClock(), config, consumers, accumulatorConsumer, executorService)
			
			return consumers;
		}
	}
}
