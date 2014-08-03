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

import com.haines.ml.rce.eventstream.NetworkChannelProcessor;
import com.haines.ml.rce.eventstream.NetworkChannelProcessor.NetworkChannelProcessorProvider;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;

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
			
			return loadJaxbClass(defaultLocation);
		}
		
		private RCEConfig loadJaxbClass(InputStream stream) throws JAXBException{
			
			if (stream == null){
				throw new IllegalArgumentException("jaxb config file could not be found");
			}
			
			JAXBContext context = JAXBContext.newInstance(RCEConfigJAXB.class);
			
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			return (RCEConfig)unmarshaller.unmarshal(stream);
		}
		
	}
}
