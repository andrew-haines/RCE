package com.haines.ml.rce.eventstream;

public interface EventStreamController {

	void start() throws EventStreamException;
	
	void stop() throws EventStreamException;
	
	boolean isAlive();
}
