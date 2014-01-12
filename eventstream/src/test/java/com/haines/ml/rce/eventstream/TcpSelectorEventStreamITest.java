package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class TcpSelectorEventStreamITest extends AbstractSelectorEventStreamIT<ServerSocketChannel>{

	@Override
	protected NetworkChannelProcessor<ServerSocketChannel> createNetworkChannelProcessor() {
		return NetworkChannelProcessor.UTIL.getServerChannelProcessor();
	}

	@Override
	protected WritableByteChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException {
		AbstractSelector selector = SelectorProvider.provider().openSelector();
		
		SocketChannel socketChannel = SocketChannel.open();
	    socketChannel.configureBlocking(true);
	  
	    // Kick off connection establishment
	    while(!socketChannel.connect(address));//16354
	    
	    socketChannel.finishConnect();
	    
	    selector.close();
	    return socketChannel;
	}
}
