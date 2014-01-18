package com.haines.ml.rce.dispatcher;

import java.util.Random;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Event;

public class Dispatcher<E extends Event> {

	private final DispatcherConsumer<E>[] consumers;
	private final Random randomGenerator;
	
	@SuppressWarnings("unchecked")
	public Dispatcher(Iterable<DispatcherConsumer<E>> consumers){
		
		this.consumers = Iterables.toArray(consumers, DispatcherConsumer.class);
		this.randomGenerator = new Random();
	}

	public void dispatchEvent(E event) {
		consumers[randomGenerator.nextInt(consumers.length)].consumeEvent(event);
	}
}
