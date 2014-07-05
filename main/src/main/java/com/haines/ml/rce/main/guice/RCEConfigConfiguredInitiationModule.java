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
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.dispatcher.DispatcherConsumer;
import com.haines.ml.rce.dispatcher.DisruptorConfig;
import com.haines.ml.rce.dispatcher.DisruptorConsumer;
import com.haines.ml.rce.eventstream.EventStream;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor;
import com.haines.ml.rce.eventstream.SelectorEventStream;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.SelectorEventStreamConfigBuilder;
import com.haines.ml.rce.eventstream.SelectorEventStreamFactory;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor.NetworkChannelProcessorProvider;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventConsumerFactory;
import com.haines.ml.rce.model.EventMarshalBuffer;

public abstract class RCEConfigConfiguredInitiationModule<E extends Event> extends AbstractModule{

	private static final String PRIMARY_CONSUMER_BIND_KEY = "primaryConsumer";
	private final String overrideLocation;
	
	public RCEConfigConfiguredInitiationModule(String overrideLocation){
		this.overrideLocation = overrideLocation;
	}
	
	@Override
	protected void configure() {
		try{
			bind(RCEApplication.class).in(Scopes.SINGLETON);
			bind(SelectorEventStreamConfig.class).toProvider(SelectorEventStreamConfigProvider.class).in(Scopes.SINGLETON);
			bind(RCEConfig.class).toInstance(loadConfig(overrideLocation));
			bind(new TypeLiteral<EventStream>(){}).to(getSelectorEventStreamType()).in(Scopes.SINGLETON); // TODO maybe not singleton if we want multiple selectors but lets think about this some more
			bind(NetworkChannelProcessorProvider.class).toProvider(getNetworkChannelProcessorProviderType()).in(Scopes.SINGLETON);
			bind(Dispatcher.class).in(Scopes.SINGLETON);
			bind(new TypeLiteral<Iterable<? extends DispatcherConsumer<E>>>(){}).toProvider(getDispatcherConsumersProviderType()).in(Scopes.SINGLETON);
			bind(new TypeLiteral<EventConsumerFactory<E, ? extends EventConsumer<E>>>(){}).annotatedWith(Names.named(PRIMARY_CONSUMER_BIND_KEY)).to(getEventConsumerFactoryType()).in(Scopes.SINGLETON);
		} catch (JAXBException | IOException e){
			throw new RuntimeException("Unable to configure RCE module from JAXB config", e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Class<? extends Provider<? extends Iterable<? extends DispatcherConsumer<E>>>> getDispatcherConsumersProviderType() {
		return (Class)DisruptorDispatcherConsumersProvider.class;
	}

	protected Class<? extends Provider<? extends NetworkChannelProcessorProvider<?>>> getNetworkChannelProcessorProviderType(){
		return NetworkChannelProcessorGuiceProvider.class;
	}
	
	protected abstract Class<? extends EventConsumerFactory<E, ? extends EventConsumer<E>>> getEventConsumerFactoryType();
	
	private static RCEConfig loadConfig(String overrideLocation) throws JAXBException, IOException {
		Path overrideLocationPath = null;
		if (overrideLocation == null){
			overrideLocationPath = Paths.get(overrideLocation);
		}
		
		return RCEConfig.UTIL.loadConfig(overrideLocationPath);
	}

	/**
	 * Override if you require a custom event stream provider
	 * @return
	 */
	protected Class<? extends EventStream> getSelectorEventStreamType() {
		return SelectorEventStream.class;
	}
	
	public static class SelectorEventStreamConfigProvider implements Provider<SelectorEventStreamConfig>{

		private final RCEConfig config;
		
		@Inject
		public SelectorEventStreamConfigProvider(RCEConfig config){
			this.config = config;
		}
		@Override
		public SelectorEventStreamConfig get() {
			SelectorEventStreamConfigBuilder configBuilder = new SelectorEventStreamConfigBuilder()
																.socketAddress(config.getEventStreamSocketAddress());
			
			if (config.getEventBufferCapacity() != null){
				configBuilder.bufferCapacity(config.getEventBufferCapacity());
			}
			
			if (config.getEventBufferType() != null){
				configBuilder.bufferType(config.getEventBufferType());
			}
			
			if (config.getByteOrder() != null){
				configBuilder.byteOrder(config.getByteOrder());
			}
												
			return configBuilder.build();									
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
			switch (config.getEventTransportProtocal()){
				case TCP:{
					return NetworkChannelProcessor.TCP_PROVIDER;
				} case UDP:{
					return NetworkChannelProcessor.UDP_PROVIDER;
				} default:{
					throw new IllegalArgumentException("unknown stream type: "+config.getEventTransportProtocal());
				}
			}
		}
		
	}
	
	public static class SelectorEventStreamProvider<T extends SelectableChannel & NetworkChannel> implements Provider<SelectorEventStream<T>>{

		private final SelectorEventStreamConfig config;
		private final NetworkChannelProcessorProvider<T> networkProcessorProvider;
		private final Dispatcher<?> dispatcher;
		private final EventMarshalBuffer<?> marshalBuffer;
		private final EventStreamListener listener;
		
		@Inject
		public SelectorEventStreamProvider(SelectorEventStreamConfig config, 
										   NetworkChannelProcessorProvider<T> networkProcessorProvider,
										   Dispatcher<?> dispatcher,
										   EventMarshalBuffer<?> marshalBuffer,
										   EventStreamListener listener){
			this.config = config;
			this.networkProcessorProvider = networkProcessorProvider;
			this.dispatcher = dispatcher;
			this.marshalBuffer = marshalBuffer;
			this.listener = listener;
		}
		
		@Override
		public SelectorEventStream<T> get() {
			SelectorEventStreamFactory<T> streamFactory = new SelectorEventStreamFactory<T>(config, networkProcessorProvider.get(), marshalBuffer, listener);
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
