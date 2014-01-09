package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public interface NetworkChannelProcessor<T extends SelectableChannel & NetworkChannel> {
	
	public static final Util UTIL = new Util();

	T createChannel(SelectorProvider provider) throws IOException;
	
	void acceptChannel(Selector selector, T channel) throws IOException;
	
	public static final class Util{
		
		private Util(){}
		
		private final NetworkChannelProcessor<ServerSocketChannel> serverChannelProcessor = new NetworkChannelProcessor<ServerSocketChannel>(){

			@Override
			public ServerSocketChannel createChannel(SelectorProvider provider) throws IOException {
				return provider.openServerSocketChannel();
			}

			@Override
			public void acceptChannel(Selector selector, ServerSocketChannel channel) throws IOException {
				SocketChannel socketChannel = channel.accept();
				socketChannel.configureBlocking(false);
				
				socketChannel.register(selector, SelectionKey.OP_READ);
			}
			
		};
		
		private final NetworkChannelProcessor<DatagramChannel> datagramChannelProcessor = new NetworkChannelProcessor<DatagramChannel>(){

			@Override
			public DatagramChannel createChannel(SelectorProvider provider) throws IOException {
				return provider.openDatagramChannel();
			}

			@Override
			public void acceptChannel(Selector selector, DatagramChannel channel) throws IOException {
				
			}
			
		};
		
		public NetworkChannelProcessor<ServerSocketChannel> getServerChannelProcessor(){
			return serverChannelProcessor;
		}
		
		public NetworkChannelProcessor<DatagramChannel> getDatagramChannelProcessor(){
			return datagramChannelProcessor;
		}
	}
}
