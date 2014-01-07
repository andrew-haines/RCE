package com.haines.ml.rce.eventstream;

public class EventStreamException extends Exception {

	EventStreamException(String message, Throwable cause){
		super(message, cause);
	}
	
	EventStreamException(String message){
		super(message);
	}
}
