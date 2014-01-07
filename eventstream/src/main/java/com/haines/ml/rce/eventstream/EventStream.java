package com.haines.ml.rce.eventstream;

import com.haines.ml.rce.dispatcher.Dispatcher;

public interface EventStream {

	void start() throws EventStreamException;
	
	void stop() throws EventStreamException;
	
	boolean isRunning();
}
