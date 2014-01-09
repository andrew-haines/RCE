package com.haines.ml.rce.eventstream;

public interface EventStream {

	void start() throws EventStreamException;
	
	void stop() throws EventStreamException;
	
	boolean isAlive();
}
