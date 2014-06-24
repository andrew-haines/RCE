package com.haines.ml.rce.eventstream;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventMarshalBuffer;

public class SelectorEventStreamFactory<T extends SelectableChannel & NetworkChannel> implements EventStreamFactory{

	private final NetworkChannelProcessor<T> channelFactory;
	private final SelectorEventStreamConfig config;
	private final EventMarshalBuffer<?> eventBuffer;
	private final EventStreamListener listener;
	
	public SelectorEventStreamFactory(SelectorEventStreamConfig config, NetworkChannelProcessor<T> channelFactory, EventMarshalBuffer<?> eventBuffer, EventStreamListener listener){
		this.channelFactory = channelFactory;
		this.config = config;
		this.eventBuffer = eventBuffer;
		this.listener = listener;
	}
	
	public SelectorEventStreamFactory(SelectorEventStreamConfig config, NetworkChannelProcessor<T> channelFactory, EventMarshalBuffer<?> eventBuffer){
		this(config, channelFactory, eventBuffer, EventStreamListener.NO_OP_LISTENER);
	}
	
	@Override
	public SelectorEventStream<T> create(Dispatcher<?> dispatcher) {
		return genericCreate(dispatcher);
	}
	
	@SuppressWarnings("unchecked")
	private <E extends Event> SelectorEventStream<T> genericCreate(Dispatcher<E> dispatcher){
		return new SelectorEventStream<T>(dispatcher, config, channelFactory, (EventMarshalBuffer<E>)eventBuffer, listener);
	}

}
