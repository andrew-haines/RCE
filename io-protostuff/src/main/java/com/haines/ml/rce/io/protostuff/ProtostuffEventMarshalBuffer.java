package com.haines.ml.rce.io.protostuff;

import java.nio.ByteBuffer;

import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Schema;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.UnMarshalableException;

public class ProtostuffEventMarshalBuffer<T extends Message<T> & Event> implements EventMarshalBuffer<T>{

	private final Schema<T> schema;
	private T messageBuffer;
	
	public ProtostuffEventMarshalBuffer(Schema<T> schema){
		this.schema = schema;
		this.messageBuffer = schema.newMessage();
	}
	
	@Override
	public boolean marshal(ByteBuffer content) {
		
		//schema.mergeFrom(input, messageBuffer);
		
		return schema.isInitialized(messageBuffer);
	}

	@Override
	public T buildEventAndResetBuffer() throws UnMarshalableException {
		
		T returnValue = this.messageBuffer;
		
		this.messageBuffer = schema.newMessage();
		return returnValue;
	}

}
