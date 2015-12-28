package com.haines.ml.rce.model;

/**
 * Creates an event consumer
 * @author haines
 *
 * @param <E> The type of the event
 * @param <EC> The type of the event consumer this factory creates
 */
public interface EventConsumerFactory<E extends Event, EC extends EventConsumer<E>> {

	/**
	 * Creates an instance of the event consumer
	 * @return
	 */
	EC create();
}
