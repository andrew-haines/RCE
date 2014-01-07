package com.haines.ml.rce.eventstream;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;

import com.haines.ml.rce.dispatcher.Dispatcher;

public class SelectorEventStreamFactory<T extends SelectableChannel & NetworkChannel> implements EventStreamFactory{

	private final NetworkChannelProcessor<T> channelFactory;
	private final SelectorEventStreamConfig config;
	
	public SelectorEventStreamFactory(SelectorEventStreamConfig config, NetworkChannelProcessor<T> channelFactory){
		this.channelFactory = channelFactory;
		this.config = config;
	}
	
	@Override
	public EventStream create(Dispatcher dispatcher) {
		return new SelectorEventStream<T>(dispatcher, config, channelFactory);
	}

}
