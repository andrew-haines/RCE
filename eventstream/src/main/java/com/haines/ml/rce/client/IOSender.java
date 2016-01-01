package com.haines.ml.rce.client;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface IOSender {
	
	public void write(ByteBuffer data) throws IOException;
	
	public static final Logger LOG = LoggerFactory.getLogger(IOSender.class);
	public static final Factory FACTORY = new Factory();
	
	public final static class Factory{
		
		private Factory(){}
		
	
		public SocketChannel initiateConnection(SocketAddress eventStreamSocketAddress, Selector selector) throws IOException {
			
			LOG.info("Creating a new connection to server: "+eventStreamSocketAddress);
			// Create a non-blocking socket channel
			SocketChannel socketChannel = SocketChannel.open();
			configureChannel(socketChannel);
		
			// Kick off connection establishment
			socketChannel.connect(eventStreamSocketAddress);
		
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
			
			while(selector.select() != 1){}; // busy spin until connection is made
			
			Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
			if (!selectedKeys.next().isConnectable()){
				throw new IllegalStateException("We should have recieved a connection response");
			} else{
				selectedKeys.remove();
				if (selectedKeys.hasNext()){
					throw new IllegalStateException("There are still selections requiring attention. ");
				}
			}
			if (!socketChannel.finishConnect()){
				throw new IllegalStateException("unable to finish connection");
			}
			
			if (!socketChannel.isConnected()){
				throw new IllegalStateException("connection not established");
			}
			
			return socketChannel;
		}
		
	
		private void configureChannel(SocketChannel channel) throws IOException {
		    channel.configureBlocking(false);
		    channel.socket().setSendBufferSize(0x100000); // 1Mb
		    channel.socket().setReceiveBufferSize(0x100000); // 1Mb
		    channel.socket().setKeepAlive(true);
		    channel.socket().setReuseAddress(true);
		    channel.socket().setSoLinger(false, 0);
		    channel.socket().setSoTimeout(0);
		    channel.socket().setTcpNoDelay(true);
		}
	
	
		public IOSender getUdpClientIOSender(final SocketAddress address, boolean reuseConnection) throws IOException, InterruptedException {
			IOSender sender;
			
			LOG.info("created new "+(reuseConnection?"reusable ":"")+"UDP client channel at {}", address);
			
			if (reuseConnection){
				final DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
				
				channel.connect(address);
				
				
				
				sender = new IOSender(){
		
					@Override
					public void write(ByteBuffer data) throws IOException {
						channel.write(data);
					}
				};
			} else {
				
				return new IOSender(){
		
					@Override
					public void write(ByteBuffer data) throws IOException {
						DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
						
						channel.connect(address);
						channel.write(data);
						channel.close();
					}
				};
			}
			
			return sender;
		}
	
		public IOSender getTcpClientIOSender(SocketAddress eventStreamSocketAddress, final boolean closeAfterEachInvocation) throws IOException {
			
			LOG.info("creating TCP client channel provider for {}", eventStreamSocketAddress);
			
			final Selector selector = SelectorProvider.provider().openSelector();
			
			final SocketChannel serverChannel = initiateConnection(eventStreamSocketAddress, selector);
			
	//		final SelectionKey writeKey = connectedChannel.keyFor(selector);
	//		writeKey.interestOps(SelectionKey.OP_WRITE);
			
			final SelectionKey readKey = serverChannel.keyFor(selector);
			readKey.interestOps(SelectionKey.OP_READ);
	//		
	//		final SelectionKey acceptKey = connectedChannel.keyFor(selector);
	//		acceptKey.interestOps(SelectionKey.OP_ACCEPT);
			
			final ByteBuffer readBuffer = ByteBuffer.allocate(2048);
			
			return new IOSender(){
	
				@Override
				public void write(ByteBuffer data) throws IOException {
					
					SocketChannel connectedChannel = serverChannel;
					if (!connectedChannel.isOpen()){
						throw new IllegalStateException("channel not open");
					}
					connectedChannel.write(data);
					//LOG.info("sent event");
	
					if (selector.select() == 0){
						throw new IllegalStateException("Should have recieved a response message");
					}; // specificly wait for the completed packet to come back
					
					Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
					
					while (keys.hasNext()){
						SelectionKey key = keys.next();
						keys.remove();
						
						//LOG.debug("Recieved Key for: readable("+key.isReadable()+"), writable("+key.isWritable()+"), connectable("+key.isConnectable()+"), acceptable("+key.isAcceptable()+")");
						SocketChannel socketChannel = (SocketChannel) key.channel();
						
						if (key.isReadable()){
							readBuffer.clear();
							socketChannel.read(readBuffer);
							
							readBuffer.flip();
							if (readBuffer.get() != 127 || readBuffer.position() != 1){ // success byte
								throw new IllegalStateException("response was not a success message: "+Arrays.toString(readBuffer.array()));
							}
							
							// now wait for the response from the server
							
							if (closeAfterEachInvocation){
								LOG.info("closing connection to server");
								socketChannel.close();
								serverChannel.close();
							}
						} 
					}
				}
				
			};
		}
	}
}
