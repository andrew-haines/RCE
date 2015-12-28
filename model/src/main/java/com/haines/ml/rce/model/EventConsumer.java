package com.haines.ml.rce.model;

/**
 * Provides an abstract of anything that can consume an event within the RCE system. Provides the core
 * event handling interface for passing events through the system.
 * @author haines
 *
 * @param <T> The typed event
 */
public interface EventConsumer<T extends Event> {

	/**
	 * Consumes a single event.
	 * @param event
	 */
	void consume(T event);
}
