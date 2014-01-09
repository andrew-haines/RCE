package com.haines.ml.rce.eventstream;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventBuffer;

public class SelectorEventStreamFactory<T extends SelectableChannel & NetworkChannel> implements EventStreamFactory{

	private final NetworkChannelProcessor<T> channelFactory;
	private final SelectorEventStreamConfig config;
	private final EventBuffer<?> eventBuffer;
	
	public SelectorEventStreamFactory(SelectorEventStreamConfig config, NetworkChannelProcessor<T> channelFactory, EventBuffer<?> eventBuffer){
		this.channelFactory = channelFactory;
		this.config = config;
		this.eventBuffer = eventBuffer;
	}
	
	@Override
	public EventStream create(Dispatcher<?> dispatcher) {
		return genericCreate(dispatcher);
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Event> EventStream genericCreate(Dispatcher<E> dispatcher){
		return new SelectorEventStream<T>(dispatcher, config, channelFactory, (EventBuffer<E>)eventBuffer);
	}

}
