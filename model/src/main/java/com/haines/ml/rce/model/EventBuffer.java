package com.haines.ml.rce.model;

import java.nio.ByteBuffer;

/**
 * Stateful buffer used for consuming byte buffers and building corresonding events.
 * Each call to {@link #buildEventAndResetBuffer()} returns an Event based on all previous calls to
 * {@link #marshal(ByteBuffer)} since the last call to {@link #buildEventAndResetBuffer()}. This means that
 * an instance of EventBuffer cannot be used across threads and that a call to {@link #buildEventAndResetBuffer()}
 * must reset the buffer
 * @author haines
 *
 * @param <E>
 */
public interface EventBuffer<E extends Event> {

	/**
	 * marshals this buffer of content. Expect this to be called possibly more then once before {@link #buildEventAndResetBuffer()} is invoked.
	 * If the buffer has all the data it needs to construct it's event then this should return true.
	 * 
	 * @param content the buffer to read
	 * @return  Whether the event has read enough buffers to fully create the event.
	 */
	boolean marshal(ByteBuffer content);
	
	E buildEventAndResetBuffer() throws UnMarshalableException;
	
}
