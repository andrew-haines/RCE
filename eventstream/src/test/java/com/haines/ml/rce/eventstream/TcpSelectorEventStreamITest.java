package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.haines.ml.rce.client.IOSender;

public class TcpSelectorEventStreamITest extends AbstractSelectorEventStreamIT<ServerSocketChannel, SocketChannel>{

	
	@Override
	protected NetworkChannelProcessor<ServerSocketChannel> createNetworkChannelProcessor() {
		
		return NetworkChannelProcessor.UTIL.getServerChannelProcessor();
	}

	@Override
	protected int getBufferCapacity() { // we wish to test a dynamic buffer where the event can be read over multiple buffer reads hence why the size is so small.
		return 10;
	}

	@Override
	protected IOSender getIOSender(SocketAddress address) throws IOException, InterruptedException {
		return IOSender.FACTORY.getTcpClientIOSender(address, false);
	}
}
