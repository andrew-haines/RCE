package com.haines.ml.rce.dispatcher;

import java.util.Random;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Event;

/**
 * This class dispatches events uniformly amongst an array of event consumders
 * @author haines
 *
 * @param <E>
 */
public class Dispatcher<E extends Event> {

	private final DispatcherConsumer<E>[] consumers;
	private final Random randomGenerator;
	
	@SuppressWarnings("unchecked")
	@Inject
	public Dispatcher(Iterable<DispatcherConsumer<E>> consumers){
		
		this.consumers = Iterables.toArray(consumers, DispatcherConsumer.class);
		this.randomGenerator = new Random();
	}

	public void dispatchEvent(E event) {
		consumers[randomGenerator.nextInt(consumers.length)].consumeEvent(event);
	}
	
	@SuppressWarnings("unchecked")
	public void sendHeartBeat(){
		for (DispatcherConsumer<E> consumer: consumers){
			consumer.consumeEvent((E)Event.HEARTBEAT);
		}
	}

	public void close() {
		for (DispatcherConsumer<E> consumer: consumers){
			consumer.shutdown();
		}
	}
}
