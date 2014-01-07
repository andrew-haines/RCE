package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicBoolean;

import com.haines.ml.rce.dispatcher.Dispatcher;

/**
 * A nio selector event stream for processing io events to the dispatcher. This selector is
 * intended but not enforced to be run in a seperate thread and it's {@link #start()} method will
 * block until an appropriate call to {@link #stop()}
 * @author haines
 *
 */
class SelectorEventStream implements EventStream{

	private final AtomicBoolean isRunning;
	private final SelectorEventStreamConfig config;
	private final Dispatcher dispatcher;
	private final NetworkChannelFactory factory;
	
	SelectorEventStream(Dispatcher dispatcher, SelectorEventStreamConfig config, NetworkChannelFactory factory){
		this.isRunning = new AtomicBoolean(false);
		this.dispatcher = dispatcher;
		this.config = config;
		this.factory = factory;
	}
	
	@Override
	public void start() throws EventStreamException{
		
		try{
			SelectorProvider provider = SelectorProvider.provider();
			Selector socketSelector = provider.openSelector();
			
			NetworkChannel channel = factory.createChannel(provider);
			ByteBuffer buffer = createBuffer();
			
		} catch (IOException e){
			throw new EventStreamException("Unable to start selector event stream", e);
		}
	}
	
	private ByteBuffer createBuffer() throws EventStreamException{
		
		ByteBuffer buffer;
		
		switch(config.getBufferType()){
			case DIRECT_BUFFER:{
				buffer = ByteBuffer.allocateDirect(config.getBufferCapacity());
				break;
			}
			case HEAP_BUFFER:{
				buffer = ByteBuffer.allocate(config.getBufferCapacity());
				break;
			} default:{
				throw new EventStreamException("Unable to start event stream as the config has specified an unknown buffer type: "+config.getBufferType());
			}
		}
		
		return buffer;
	}
	
	@Override
	public void stop() throws EventStreamException{
		isRunning.set(false);
	}
	
	@Override
	public boolean isRunning(){
		return isRunning.get();
	}
}
