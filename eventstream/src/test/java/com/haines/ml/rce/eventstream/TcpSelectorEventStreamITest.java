package com.haines.ml.rce.eventstream;

import java.nio.channels.ServerSocketChannel;

public class TcpSelectorEventStreamITest extends AbstractSelectorEventStreamIT<ServerSocketChannel>{

	@Override
	protected NetworkChannelProcessor<ServerSocketChannel> createNetworkChannelProcessor() {
		return NetworkChannelProcessor.UTIL.getServerChannelProcessor();
	}

}
