package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class TcpSelectorEventStreamITest extends AbstractSelectorEventStreamIT<ServerSocketChannel, SocketChannel>{

	@Override
	protected NetworkChannelProcessor<ServerSocketChannel> createNetworkChannelProcessor() {
		return NetworkChannelProcessor.UTIL.getServerChannelProcessor();
	}

	@Override
	protected SocketChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException {
		AbstractSelector selector = SelectorProvider.provider().openSelector();
		
		SocketChannel socketChannel = SelectorProvider.provider().openSocketChannel();
	    socketChannel.configureBlocking(true);
	  
	    // Kick off connection establishment
	    while(!socketChannel.connect(address));//16354
	    
	    socketChannel.finishConnect();
	    
	    selector.close();
	    return socketChannel;
	}

	@Override
	protected int getBufferCapacity() { // we wish to test a dynamic buffer where the event can be read over multiple buffer reads hence why the size is so small.
		return 2048;
	}
}
