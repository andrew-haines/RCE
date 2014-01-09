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

	void marshal(ByteBuffer content);
	
	E buildEventAndResetBuffer() throws UnMarshalableException;
	
}
