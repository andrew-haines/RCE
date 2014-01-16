package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

public interface NetworkChannelProcessor<T extends SelectableChannel & NetworkChannel> {
	
	public static final Util UTIL = new Util();

	T createChannel(SelectorProvider provider) throws IOException;
	
	int getRegisterOpCodes();
	
	void acceptChannel(Selector selector, T channel) throws IOException;
	
	void connect(SocketAddress address, T channel) throws IOException;
	
	int readFromChannel(ScatteringByteChannel readerChannel, ByteBuffer buffer) throws IOException;
	
	void close(T channel) throws IOException;

	void closeAccept(ScatteringByteChannel readerChannel) throws IOException;
	
	public static final class Util{
		
		private Util(){}
		
		private final NetworkChannelProcessor<ServerSocketChannel> serverChannelProcessor = new NetworkChannelProcessor<ServerSocketChannel>(){

			@Override
			public ServerSocketChannel createChannel(SelectorProvider provider) throws IOException {
				ServerSocketChannel channel = provider.openServerSocketChannel();
				
				return channel;
			}

			@Override
			public void acceptChannel(Selector selector, ServerSocketChannel channel) throws IOException {
				SocketChannel socketChannel = channel.accept();
				socketChannel.configureBlocking(false);
				
				socketChannel.finishConnect();
				socketChannel.register(selector, SelectionKey.OP_READ);
			}

			@Override
			public int getRegisterOpCodes() {
				return SelectionKey.OP_ACCEPT;
			}

			@Override
			public void close(ServerSocketChannel channel) throws IOException {
				channel.close();
			}

			@Override
			public void connect(SocketAddress address, ServerSocketChannel channel) throws IOException {
				channel.bind(address);
			}

			@Override
			public int readFromChannel(ScatteringByteChannel readerChannel, ByteBuffer buffer) throws IOException {
				return readerChannel.read(buffer);
			}

			@Override
			public void closeAccept(ScatteringByteChannel readerChannel) throws IOException {
				readerChannel.close();
			}
			
		};
		
		private final NetworkChannelProcessor<DatagramChannel> datagramChannelProcessor = new NetworkChannelProcessor<DatagramChannel>(){

			@Override
			public DatagramChannel createChannel(SelectorProvider provider) throws IOException {
				DatagramChannel channel = provider.openDatagramChannel(StandardProtocolFamily.INET);
				channel.configureBlocking(false);
				
				return channel;
			}

			@Override
			public void acceptChannel(Selector selector, DatagramChannel channel) throws IOException {
				throw new UnsupportedOperationException("Accept does not mean anything for a datagram packet");
			}

			@Override
			public int getRegisterOpCodes() {
				return SelectionKey.OP_READ;
			}

			@Override
			public void close(DatagramChannel channel) throws IOException {
				channel.disconnect();
				channel.close();
			}

			@Override
			public void connect(SocketAddress address, DatagramChannel channel) throws IOException {
				
				channel.bind(address);
			}

			@Override
			public int readFromChannel(ScatteringByteChannel readerChannel, ByteBuffer buffer) throws IOException {
				int position = buffer.position();
				((DatagramChannel)readerChannel).receive(buffer);
				
				return buffer.position() - position;
			}

			@Override
			public void closeAccept(ScatteringByteChannel readerChannel)throws IOException {
				// socket does not need closing for datagrams
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
