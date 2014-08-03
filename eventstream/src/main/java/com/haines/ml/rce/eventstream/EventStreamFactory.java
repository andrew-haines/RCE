package com.haines.ml.rce.eventstream;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;

public interface EventStreamFactory<E extends Event> {

	EventStream create(Dispatcher<E> dispatcher);
}
