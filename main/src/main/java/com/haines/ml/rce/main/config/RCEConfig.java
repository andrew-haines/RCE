package com.haines.ml.rce.main.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.haines.ml.rce.accumulator.AccumulatorConfig;
import com.haines.ml.rce.accumulator.PipelineAccumulatorConfig;
import com.haines.ml.rce.dispatcher.DisruptorConfig;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor.NetworkChannelProcessorProvider;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.SelectorEventStreamConfigBuilder;
import com.haines.ml.rce.main.config.jaxb.RCEConfigJAXB;
import com.haines.ml.rce.window.WindowConfig;

public interface RCEConfig {
	
	public static final Util UTIL = new Util();
	
	public static enum StreamType {
		UDP(NetworkChannelProcessor.UDP_PROVIDER),
		TCP(NetworkChannelProcessor.TCP_PROVIDER);
		
		private final NetworkChannelProcessorProvider<?> provider;
		
		private StreamType(NetworkChannelProcessorProvider<?> provider){
			this.provider = provider;
		}

		public NetworkChannelProcessorProvider<?> getChannelProcessorProvider() {
			return provider;
		}
		
		public static StreamType fromValue(String value){
			for (StreamType type: values()){
				if (type.name().equalsIgnoreCase(value)){
					return type;
				}
			}
			
			throw new IllegalArgumentException("Unknow stream type: "+value);
		}
	}

	Integer getNumberOfEventWorkers();
	
	StreamType getEventTransportProtocal();
	
	Integer getEventBufferCapacity();
	
	BufferType getEventBufferType();
	
	ByteOrder getByteOrder();
	
	SocketAddress getEventStreamSocketAddress();
	
	Integer getFirstAccumulatorLineBitDepth();
	
	Integer getSecondAccumulatorLineBitDepth();
	
	Integer getFinalAccumulatorLineBitDepth();
	
	long getMicroBatchIntervalMs();
	
	public static class Util{
		
		private static final String DEFAULT_CONFIG_LOC = "/xml/default-config.xml";
		
		public RCEConfig loadConfig(Path configOverrideLocation) throws JAXBException, IOException{
			RCEConfig config = getDefaultConfig();
			
			if (configOverrideLocation != null){
				config = new OverrideRCEConfig(config, getOverrideConfig(configOverrideLocation));
			}
			
			return config;
		}

		private RCEConfig getOverrideConfig(Path configOverrideLocation) throws IOException, JAXBException {
			InputStream stream = Files.newInputStream(configOverrideLocation, StandardOpenOption.READ);
			
			return loadJaxbClass(stream);
		}

		private RCEConfig getDefaultConfig() throws JAXBException {
			InputStream defaultLocation = Util.class.getResourceAsStream(DEFAULT_CONFIG_LOC);
			
			return new DefaultRCEConfig(loadJaxbClass(defaultLocation));
		}
		
		private RCEConfig loadJaxbClass(InputStream stream) throws JAXBException{
			
			if (stream == null){
				throw new IllegalArgumentException("jaxb config file could not be found");
			}
			
			JAXBContext context = JAXBContext.newInstance(RCEConfigJAXB.class);
			
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			return (RCEConfig)unmarshaller.unmarshal(stream);
		}
		
		public SelectorEventStreamConfig getSelectorEventStreamConfig(RCEConfig config){
			SelectorEventStreamConfigBuilder configBuilder = new SelectorEventStreamConfigBuilder()
			.socketAddress(config.getEventStreamSocketAddress())
			.heartBeatPeriod(config.getMicroBatchIntervalMs()); // if this is a sync call, we use the heart beat to trigger the microbatch if no events have been seen

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

		public NetworkChannelProcessorProvider<?> getNetworkChannelProcessorProvider(RCEConfig config) {
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
		
		public DisruptorConfig getDisruptorConfig(RCEConfig config){
			
			return new DisruptorConfig.Builder().ringSize(config.getDisruptorRingSize()).build();
		}
		
		public PipelineAccumulatorConfig getPipelineAccumulatorConfig(final RCEConfig config){
			return new PipelineAccumulatorConfig() {
				
				@Override
				public long getPushIntervalTimeMs() {
					return config.getMicroBatchIntervalMs();
				}
			};
		}

		public AccumulatorConfig getAccumulatorConfig(final RCEConfig config) {
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

		public WindowConfig getWindowConfig(final RCEConfig config) {
			return new WindowConfig(){

				@Override
				public long getWindowPeriod() {
					return config.getWindowPeriod();
				}

				@Override
				public int getNumWindows() {
					return config.getNumWindows();
				}

				@Override
				public int getGlobalIndexLimit() {
					return config.getGlobalIndexLimit();
				}
				
			};
		}
	}
	
	/**
	 * Provides programmatic default values. Used to dynamically compute the default number of workers based
	 * on the number of CPU cores available to the system
	 * @author haines
	 *
	 */
	public static class DefaultRCEConfig implements RCEConfig{

		private final RCEConfig delegate;
		
		public DefaultRCEConfig(RCEConfig delegate){
			this.delegate = delegate;
		}
		
		@Override
		public Integer getNumberOfEventWorkers() {
			return Runtime.getRuntime().availableProcessors() - 1; // 1 cpu should be used for the event dispatching
		}

		@Override
		public StreamType getEventTransportProtocal() {
			return delegate.getEventTransportProtocal();
		}

		@Override
		public Integer getEventBufferCapacity() {
			return delegate.getEventBufferCapacity();
		}

		@Override
		public BufferType getEventBufferType() {
			return delegate.getEventBufferType();
		}

		@Override
		public ByteOrder getByteOrder() {
			return delegate.getByteOrder();
		}

		@Override
		public SocketAddress getEventStreamSocketAddress() {
			return delegate.getEventStreamSocketAddress();
		}

		@Override
		public Integer getFirstAccumulatorLineBitDepth() {
			return delegate.getFirstAccumulatorLineBitDepth();
		}

		@Override
		public Integer getSecondAccumulatorLineBitDepth() {
			return delegate.getSecondAccumulatorLineBitDepth();
		}

		@Override
		public Integer getFinalAccumulatorLineBitDepth() {
			return delegate.getFinalAccumulatorLineBitDepth();
		}

		@Override
		public int getDisruptorRingSize() {
			return delegate.getDisruptorRingSize();
		}

		@Override
		public long getMicroBatchIntervalMs() {
			return delegate.getMicroBatchIntervalMs();
		}

		@Override
		public int getNumWindows() {
			return delegate.getNumWindows();
		}

		@Override
		public long getWindowPeriod() {
			return delegate.getWindowPeriod();
		}

		@Override
		public int getGlobalIndexLimit() {
			return delegate.getGlobalIndexLimit();
		}
		
	}

	int getDisruptorRingSize();

	int getGlobalIndexLimit();

	int getNumWindows();

	long getWindowPeriod();
}
