package com.haines.ml.rce.main.guice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.eventstream.SelectorEventStream;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.SelectorEventStreamConfigBuilder;
import com.haines.ml.rce.eventstream.SelectorEventStreamFactory;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor.NetworkChannelProcessorProvider;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.EventMarshalBuffer;

public class RCEConfigConfiguredInitiationModule extends AbstractModule{

	private final String overrideLocation;
	
	public RCEConfigConfiguredInitiationModule(String overrideLocation){
		this.overrideLocation = overrideLocation;
	}
	
	@Override
	protected void configure() {
		try{
			bind(RCEApplication.class).toProvider(RCEApplicationProvider.class);
			bind(SelectorEventStreamConfig.class).toProvider(SelectorEventStreamConfigProvider.class);
			bind(RCEConfig.class).toInstance(loadConfig(overrideLocation));
			bind(new TypeLiteral<SelectorEventStream<?>>(){}).toProvider(getSelectorEventStreamProvider());
			//bind(NetworkChannelProcessorProvider.class).to
		} catch (JAXBException | IOException e){
			throw new RuntimeException("Unable to configure RCE module from JAXB config", e);
		}
	}
	
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
	protected Class<? extends Provider<? extends SelectorEventStream<?>>> getSelectorEventStreamProvider() {
		//return SelectorEventStreamProvider.class;
		return null;
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

	public static class RCEApplicationProvider implements Provider<RCEApplication>{

		private final SelectorEventStream<?> eventStream;
		
		public RCEApplicationProvider(SelectorEventStream<?> eventStream){
			this.eventStream = eventStream;
		}
		@Override
		public RCEApplication get() {
			return new RCEApplication(eventStream);
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
}
