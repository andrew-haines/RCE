package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.SelectorProvider;

public class TcpSelectorEventStreamITest extends AbstractSelectorEventStreamIT<ServerSocketChannel>{

	@Override
	protected NetworkChannelProcessor<ServerSocketChannel> createNetworkChannelProcessor() {
		return NetworkChannelProcessor.UTIL.getServerChannelProcessor();
	}

	@Override
	protected WritableByteChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException {
		SelectorProvider.provider().openSelector();
		
		SocketChannel socketChannel = SocketChannel.open();
	    socketChannel.configureBlocking(false);
	  
	    // Kick off connection establishment
	    socketChannel.connect(address);
	    
	    while(!socketChannel.finishConnect()){
	    	Thread.sleep(50);
	    }
	    
	    return socketChannel;
	}

	@Override
	protected void finishChannel(WritableByteChannel channel) throws IOException {
		((SocketChannel)channel).finishConnect();
	}

}
