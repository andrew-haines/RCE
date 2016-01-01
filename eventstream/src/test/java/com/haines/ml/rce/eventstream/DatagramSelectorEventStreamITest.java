package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

import com.haines.ml.rce.client.IOSender;

public class DatagramSelectorEventStreamITest extends AbstractSelectorEventStreamIT<DatagramChannel, DatagramChannel>{

	@Override
	protected NetworkChannelProcessor<DatagramChannel> createNetworkChannelProcessor() {
		return NetworkChannelProcessor.UTIL.getDatagramChannelProcessor();
	}

	@Override
	protected int getBufferCapacity() {
		return 64; // with udp we cannot span the event packet over multiple reads from a buffer so this needs to be the maxiumum size of the udp packet
	}

	@Override
	protected int getNumberOfEvents() {
		return 100000;
	}

	@Override
	protected IOSender getIOSender(SocketAddress address) throws IOException, InterruptedException {
		return IOSender.FACTORY.getUdpClientIOSender(address, false);
	}
}
