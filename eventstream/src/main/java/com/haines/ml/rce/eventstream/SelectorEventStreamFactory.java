package com.haines.ml.rce.eventstream;

import com.haines.ml.rce.dispatcher.Dispatcher;

public class SelectorEventStreamFactory implements EventStreamFactory{

	private final NetworkChannelFactory channelFactory;
	private final SelectorEventStreamConfig config;
	
	public SelectorEventStreamFactory(SelectorEventStreamConfig config, NetworkChannelFactory channelFactory){
		this.channelFactory = channelFactory;
		this.config = config;
	}
	
	@Override
	public EventStream create(Dispatcher dispatcher) {
		return new SelectorEventStream(dispatcher, config, channelFactory);
	}

}
