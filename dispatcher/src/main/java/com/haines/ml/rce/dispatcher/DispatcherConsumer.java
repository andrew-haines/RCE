package com.haines.ml.rce.dispatcher;

import com.haines.ml.rce.model.Event;

public interface DispatcherConsumer<T extends Event> {

	void consumeEvent(T event);
}
