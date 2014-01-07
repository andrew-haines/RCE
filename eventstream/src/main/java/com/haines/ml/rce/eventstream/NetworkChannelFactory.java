package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.spi.SelectorProvider;

public interface NetworkChannelFactory {
	
	public static final Util UTIL = new Util();

	NetworkChannel createChannel(SelectorProvider provider) throws IOException;
	
	public static final class Util{
		
		private Util(){}
		
		private final NetworkChannelFactory serverChannelFactory = new NetworkChannelFactory(){

			@Override
			public NetworkChannel createChannel(SelectorProvider provider) throws IOException {
				return provider.openServerSocketChannel();
			}
			
		};
		
		private final NetworkChannelFactory datagramChannelFactory = new NetworkChannelFactory(){

			@Override
			public NetworkChannel createChannel(SelectorProvider provider) throws IOException {
				return provider.openDatagramChannel();
			}
			
		};
		
		public NetworkChannelFactory getServerChannelFactory(){
			return serverChannelFactory;
		}
		
		public NetworkChannelFactory getDatagramChannelFactory(){
			return datagramChannelFactory;
		}
	}
}
