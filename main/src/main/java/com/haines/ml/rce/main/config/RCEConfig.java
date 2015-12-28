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

/**
 * A config object that defines the entire configuration parameters of the system.
 * @author haines
 *
 */
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

	/** 
	 * The number of worker threads in the system. This should be tuned to (|cpu| - 1) so that we have exactly 1 
	 * worker assigned to each CPU core
	 * @return
	 */
	Integer getNumberOfEventWorkers();
	
	/**
	 * Whether the system should use TCP or UDP or another transport protocol.
	 * @return
	 */
	StreamType getEventTransportProtocal();
	
	/**
	 * The capacity of the buffer used to serialise each event. The larger the capacity the more data can read of the network card
	 * at once but too big and unneccessary off-heap space is allocated.
	 * @return
	 */
	Integer getEventBufferCapacity();
	
	/**
	 * Whether the system should use off or on heap memory for serialising data. off heap (direct) offers potentially direct access
	 * to the io network (DMA) bus if the underlying OS supports it which avoids unnecessary copying from DMA bus to java heap space.
	 * @return
	 */
	BufferType getEventBufferType();
	
	/**
	 * The endian ordering of the bytes that data coming into the system is organised with
	 * @return
	 */
	ByteOrder getByteOrder();
	
	/**
	 * The address of the socket that is listening to receive data into the system
	 * @return
	 */
	SocketAddress getEventStreamSocketAddress();
	
	/**
	 * The number of bits used in the first accumulator line
	 * @return
	 */
	Integer getFirstAccumulatorLineBitDepth();
	
	/**
	 * The number of bits used in the second accumulator line
	 * @return
	 */
	Integer getSecondAccumulatorLineBitDepth();
	
	/**
	 * The number of bits used in the last accumulator line
	 * @return
	 */
	Integer getFinalAccumulatorLineBitDepth();
	
	/**
	 * How long in ms should the system wait before accumulating events together. The larger the value the less pauses are present in
	 * the system and the less overal amount of space is needed to represent a given model but at the cost of not updating the system 
	 * till this time is elapsed and therefore reducing it's 'realtime' properties
	 * @return
	 */
	Long getMicroBatchIntervalMs();
	
	/**
	 * The size of the disruptor ring used to queue the events as they arrive into the system.
	 * @return
	 */
	Integer getDisruptorRingSize();

	/**
	 * Where the global name space starts. The larger the value the more room you have for new, unseen event value/type at the cost
	 * of how many type/values you can store in the global index
	 * @return
	 */
	Integer getGlobalIndexLimit();

	/**
	 * The number of windows to store. Based on the {@link #getWindowPeriod()} this determines how long the whole model is representative
	 * for. It's total time period can be determined using {@link #getNumWindows()} * {@link #getWindowPeriod()}. The larger the value
	 * the longer the model can be representative for at the expense of a linearly increasing memory foortprint.
	 * @return
	 */
	Integer getNumWindows();

	/**
	 * How long each window should be valid for.
	 * @return
	 */
	Long getWindowPeriod();
	
	public static class Util{
		
		private static final String DEFAULT_CONFIG_LOC = "/xml/default-config.xml";
		
		public RCEConfig loadConfig() throws JAXBException{
			return getDefaultConfig();
		}
		
		public RCEConfig loadConfig(Path configOverrideLocation) throws JAXBException, IOException{
			RCEConfig config = getDefaultConfig();
			
			if (configOverrideLocation != null){
				config = new OverrideRCEConfig(config, getOverrideConfig(configOverrideLocation));
			}
			
			return config;
		}
		
		public RCEConfig loadConfig(RCEConfig config) throws JAXBException, IOException{
			RCEConfig defaultConfig = getDefaultConfig();
			
			if (config != null){
				config = new OverrideRCEConfig(defaultConfig, config);
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
			if (delegate != null){
				return delegate.getEventTransportProtocal();
			} else{
				return null;
			}
		}

		@Override
		public Integer getEventBufferCapacity() {
			if (delegate != null){
				return delegate.getEventBufferCapacity();
			} else{
				return null;
			}
		}

		@Override
		public BufferType getEventBufferType() {
			if (delegate != null){
				return delegate.getEventBufferType();
			} else{
				return null;
			}
		}

		@Override
		public ByteOrder getByteOrder() {
			if (delegate != null){
				return delegate.getByteOrder();
			} else{
				return null;
			}
		}

		@Override
		public SocketAddress getEventStreamSocketAddress() {
			if (delegate != null){
				return delegate.getEventStreamSocketAddress();
			} else{
				return null;
			}
		}

		@Override
		public Integer getFirstAccumulatorLineBitDepth() {
			if (delegate != null){
				return delegate.getFirstAccumulatorLineBitDepth();
			} else{
				return null;
			}
		}

		@Override
		public Integer getSecondAccumulatorLineBitDepth() {
			if (delegate != null){
				return delegate.getSecondAccumulatorLineBitDepth();
			} else{
				return null;
			}
		}

		@Override
		public Integer getFinalAccumulatorLineBitDepth() {
			if (delegate != null){
				return delegate.getFinalAccumulatorLineBitDepth();
			} else{
				return null;
			}
		}

		@Override
		public Integer getDisruptorRingSize() {
			if (delegate != null){
				return delegate.getDisruptorRingSize();
			} else{
				return null;
			}
		}

		@Override
		public Long getMicroBatchIntervalMs() {
			if (delegate != null){
				return delegate.getMicroBatchIntervalMs();
			} else{
				return null;
			}
		}

		@Override
		public Integer getNumWindows() {
			if (delegate != null){
				return delegate.getNumWindows();
			} else{
				return null;
			}
		}

		@Override
		public Long getWindowPeriod() {
			if (delegate != null){
				return delegate.getWindowPeriod();
			} else{
				return null;
			}
		}

		@Override
		public Integer getGlobalIndexLimit() {
			if (delegate != null){
				return delegate.getGlobalIndexLimit();
			} else{
				return null;
			}
		}
		
	}
}
