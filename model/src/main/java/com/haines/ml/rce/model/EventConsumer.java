package com.haines.ml.rce.model;

public interface EventConsumer<T extends Event> {

	void consume(T event);
}
