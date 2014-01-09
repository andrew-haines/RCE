package com.haines.ml.rce.model;

public class UnMarshalableException extends RuntimeException {

	public UnMarshalableException(String message, Throwable cause){
		super(message, cause);
	}
	
	public UnMarshalableException(String message){
		super(message);
	}
}
