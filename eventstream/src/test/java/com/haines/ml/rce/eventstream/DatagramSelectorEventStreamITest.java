package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;

public class DatagramSelectorEventStreamITest extends AbstractSelectorEventStreamIT<DatagramChannel, DatagramChannel>{

	@Override
	protected NetworkChannelProcessor<DatagramChannel> createNetworkChannelProcessor() {
		return NetworkChannelProcessor.UTIL.getDatagramChannelProcessor();
	}

	@Override
	protected DatagramChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException {
		DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
		
		channel.connect(address);
		
		return channel;
	}

	@Override
	protected void sendBytes(DatagramChannel channel, ByteBuffer buffer, SocketAddress address) throws IOException {
		channel.send(buffer, address);
	}

	@Override
	protected int getBufferCapacity() {
		return 2048; // with udp we cannot span the event packet over multiple reads from a buffer so this needs to be the maxiumum size of the udp packet
	}

	
}
