package com.haines.ml.rce.io.protostuff;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.dyuproject.protostuff.ByteBufferInput;
import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Schema;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.UnMarshalableException;

public class ProtostuffEventMarshalBuffer<T extends Message<T> & Event> implements EventMarshalBuffer<T>{

	private final Schema<T> schema;
	private T messageBuffer;
	private ByteBufferInput input;
	
	public ProtostuffEventMarshalBuffer(Schema<T> schema){
		this.schema = schema;
		this.messageBuffer = schema.newMessage();
		this.input = new ByteBufferInput(true);
	}
	
	@Override
	public boolean marshal(ByteBuffer content) {
		
		input.setNextBuffer(content);
		
		byte[] buffer = new byte[content.remaining()];
		
		content.get(buffer);
		
		//ByteArrayInput input = new ByteArrayInput(buffer, 0, buffer.length, true);
		
		try {
			schema.mergeFrom(input, messageBuffer);
			
			return schema.isInitialized(messageBuffer);
		} catch (IOException e) {
			throw new RuntimeException("Unable to marshal event from buffer", e);
		}
	}

	@Override
	public T buildEventAndResetBuffer() throws UnMarshalableException {
		
		T returnValue = this.messageBuffer;
		
		this.messageBuffer = schema.newMessage();
		this.input = null;
		return returnValue;
	}

}
