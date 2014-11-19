package com.haines.ml.rce.main.guice;

import java.io.IOException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.dispatcher.DispatcherConsumer;
import com.haines.ml.rce.dispatcher.DisruptorConfig;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.eventstream.EventStreamController;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor;
import com.haines.ml.rce.eventstream.SelectorEventStream;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.SelectorEventStreamConfigBuilder;
import com.haines.ml.rce.eventstream.SelectorEventStreamFactory;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor.NetworkChannelProcessorProvider;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.RCEApplicationFactory;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventConsumerFactory;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.system.Clock;

public abstract class RCEConfigConfiguredInitiationModule<T extends SelectableChannel & NetworkChannel, E extends Event> extends AbstractModule{

	private static final String PRIMARY_CONSUMER_BIND_KEY = "primaryConsumer";
	private final String overrideLocation;
	private final Class<E> eventType;
	private final Class<T> networkChannelType;
	private final Provider<EventMarshalBuffer<E>> eventMarshallerProvider;
	
	public RCEConfigConfiguredInitiationModule(String overrideLocation, Class<E> eventType, Class<T> networkChannelType, Provider<EventMarshalBuffer<E>> eventMarshallerProvider){
		this.overrideLocation = overrideLocation;
		this.eventType = eventType;
		this.networkChannelType = networkChannelType;
		this.eventMarshallerProvider = eventMarshallerProvider;
	}
	
	@Override
	protected void configure() {
		try{
			bind(RCEApplication.class).in(Scopes.SINGLETON);
			bind(SelectorEventStreamConfig.class).toProvider(SelectorEventStreamConfigProvider.class).in(Scopes.SINGLETON);
			bind(RCEConfig.class).toInstance(RCEApplicationFactory.UTIL.loadConfig(overrideLocation));
			bind(Clock.class).to(getClockType());
			bind(EventStreamListener.class).to(getEventStreamListenerType());
			bind(EventStreamController.class).to(getSelectorEventStreamType()).in(Scopes.SINGLETON); // TODO maybe not singleton if we want multiple selectors but lets think about this some more
			bind(TypeLiteral.get(Types.newParameterizedType(NetworkChannelProcessor.class, networkChannelType))).toProvider((Class)getNetworkChannelProcessorProviderType()).in(Scopes.SINGLETON);
			bind(TypeLiteral.get(Types.newParameterizedType(EventMarshalBuffer.class, eventType))).toProvider((Provider)eventMarshallerProvider);
			bind(TypeLiteral.get(Types.newParameterizedType(Iterable.class, Types.newParameterizedType(DispatcherConsumer.class, eventType)))).toProvider((Class)getDispatcherConsumersProviderType()).in(Scopes.SINGLETON);
			bind(TypeLiteral.get(Types.newParameterizedType(Dispatcher.class, eventType))).in(Scopes.SINGLETON);
			bind(TypeLiteral.get(Types.newParameterizedType(EventConsumerFactory.class, eventType, Types.subtypeOf(Types.newParameterizedType(EventConsumer.class, eventType))))).annotatedWith(Names.named(PRIMARY_CONSUMER_BIND_KEY)).toProvider((Class)getEventConsumerFactoryProviderType()).in(Scopes.SINGLETON);
		} catch (JAXBException | IOException e){
			throw new RuntimeException("Unable to configure RCE module from JAXB config", e);
		}
	}

	private Class<? extends EventStreamListener> getEventStreamListenerType() {
		return EventStreamListener.NoOpEventStreamListener.class;
	}

	protected Class<? extends Clock> getClockType() {
		return Clock.SystemClock.class;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Class<? extends Provider<? extends Iterable<? extends DispatcherConsumer>>> getDispatcherConsumersProviderType() {
		return (Class)DisruptorDispatcherConsumersProvider.class;
	}

	protected Class<? extends Provider<? extends NetworkChannelProcessorProvider<?>>> getNetworkChannelProcessorProviderType(){
		return NetworkChannelProcessorGuiceProvider.class;
	}
	
	protected abstract Class<Provider<? extends EventConsumerFactory<E, ? extends EventConsumer<E>>>> getEventConsumerFactoryProviderType();

	/**
	 * Override if you require a custom event stream provider
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected TypeLiteral<SelectorEventStream<T, E>> getSelectorEventStreamType() {
		return (TypeLiteral<SelectorEventStream<T, E>>)TypeLiteral.get(Types.newParameterizedType(SelectorEventStream.class, networkChannelType, eventType));
	}
	
	public static class SelectorEventStreamConfigProvider implements Provider<SelectorEventStreamConfig>{

		private final RCEConfig config;
		
		@Inject
		public SelectorEventStreamConfigProvider(RCEConfig config){
			this.config = config;
		}
		@Override
		public SelectorEventStreamConfig get() {
			return RCEConfig.UTIL.getSelectorEventStreamConfig(config);								
		}
	}
	
	private static class NetworkChannelProcessorGuiceProvider implements Provider<NetworkChannelProcessorProvider<?>>{

		private final RCEConfig config;
		
		@Inject
		private NetworkChannelProcessorGuiceProvider(RCEConfig config){
			this.config = config;
		}
		
		@Override
		public NetworkChannelProcessorProvider<?> get() {
			return RCEConfig.UTIL.getNetworkChannelProcessorProvider(config);
		}
		
	}
	
	public static class SelectorEventStreamProvider<T extends SelectableChannel & NetworkChannel, E extends Event> implements Provider<SelectorEventStream<T, E>>{

		private final SelectorEventStreamConfig config;
		private final NetworkChannelProcessorProvider<T> networkProcessorProvider;
		private final Dispatcher<E> dispatcher;
		private final EventMarshalBuffer<E> marshalBuffer;
		private final EventStreamListener listener;
		
		@Inject
		public SelectorEventStreamProvider(SelectorEventStreamConfig config, 
										   NetworkChannelProcessorProvider<T> networkProcessorProvider,
										   Dispatcher<E> dispatcher,
										   EventMarshalBuffer<E> marshalBuffer,
										   EventStreamListener listener){
			this.config = config;
			this.networkProcessorProvider = networkProcessorProvider;
			this.dispatcher = dispatcher;
			this.marshalBuffer = marshalBuffer;
			this.listener = listener;
		}
		
		@Override
		public SelectorEventStream<T, E> get() {
			SelectorEventStreamFactory<T, E> streamFactory = new SelectorEventStreamFactory<T, E>(config, networkProcessorProvider.get(), marshalBuffer, listener);
			return streamFactory.create(dispatcher);
		}	
	}
	
	public static class DisruptorDispatcherConsumersProvider<E extends Event> implements Provider<Iterable<DispatcherConsumer<E>>>{

		private final RCEConfig config;
		private final DisruptorConfig disruptorConfig;
		private final EventConsumerFactory<E, ? extends EventConsumer<E>> consumerFactory;
		
		@Inject
		public DisruptorDispatcherConsumersProvider(RCEConfig config, DisruptorConfig disruptorConfig,@Named(PRIMARY_CONSUMER_BIND_KEY) EventConsumerFactory<E, ? extends EventConsumer<E>> consumerFactory){
			this.config = config;
			this.disruptorConfig = disruptorConfig;
			this.consumerFactory = consumerFactory;
		}
		@Override
		public Iterable<DispatcherConsumer<E>> get() {
			List<DispatcherConsumer<E>> workers = new ArrayList<DispatcherConsumer<E>>(config.getNumberOfEventWorkers());
			
			for (int i = 0; i < config.getNumberOfEventWorkers(); i++){
				workers.add(new DisruptorConsumer.Builder<E>(Executors.newSingleThreadExecutor(), disruptorConfig)
							.addConsumer(consumerFactory.create())
							.build());
			}
			
			return workers;
		}
	}
}
