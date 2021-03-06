package com.haines.ml.rce.eventstream;

import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;

import javax.inject.Inject;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.system.Clock;

public class SelectorEventStreamFactory<T extends SelectableChannel & NetworkChannel, E extends Event> implements EventStreamFactory<E>{

	private final NetworkChannelProcessor<T> channelFactory;
	private final SelectorEventStreamConfig config;
	private final EventMarshalBuffer<E> eventBuffer;
	private final EventStreamListener listener;
	private final Clock clock;
	
	@Inject
	public SelectorEventStreamFactory(Clock clock, SelectorEventStreamConfig config, NetworkChannelProcessor<T> channelFactory, EventMarshalBuffer<E> eventBuffer, EventStreamListener listener){
		this.channelFactory = channelFactory;
		this.config = config;
		this.eventBuffer = eventBuffer;
		this.listener = listener;
		this.clock = clock;
	}
	
	public SelectorEventStreamFactory(Clock clock, SelectorEventStreamConfig config, NetworkChannelProcessor<T> channelFactory, EventMarshalBuffer<E> eventBuffer){
		this(clock, config, channelFactory, eventBuffer, EventStreamListener.NO_OP_LISTENER);
	}
	
	@Override
	public SelectorEventStream<T, E> create(Dispatcher<E> dispatcher) {
		return genericCreate(dispatcher);
	}
	
	private SelectorEventStream<T, E> genericCreate(Dispatcher<E> dispatcher){
		return new SelectorEventStream<T, E>(clock, dispatcher, config, channelFactory, (EventMarshalBuffer<E>)eventBuffer, listener);
	}

}
