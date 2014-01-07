package com.haines.ml.rce.eventstream;

import com.haines.ml.rce.dispatcher.Dispatcher;

public interface EventStreamFactory {

	EventStream create(Dispatcher dispatcher);
}
