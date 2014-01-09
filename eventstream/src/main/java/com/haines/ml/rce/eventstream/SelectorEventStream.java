package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventBuffer;

/**
 * A nio selector event stream for processing io events to the dispatcher. This selector is
 * intended but not enforced to be run in a seperate thread and it's {@link #start()} method will
 * block until an appropriate call to {@link #stop()}
 * @author haines
 *
 */
class SelectorEventStream<T extends SelectableChannel & NetworkChannel> implements EventStream{

	private static final Logger LOG = LoggerFactory.getLogger(SelectorEventStream.class);
	
	private volatile boolean isRunning;
	private final SelectorEventStreamConfig config;
	private final Dispatcher<?> dispatcher;
	private final NetworkChannelProcessor<T> processor;
	private final EventBuffer<?> eventBuffer;
	
	<E extends Event> SelectorEventStream(Dispatcher<E> dispatcher, SelectorEventStreamConfig config, NetworkChannelProcessor<T> processor, EventBuffer<E> eventBuffer){
		this.isRunning = false;
		this.dispatcher = dispatcher;
		this.config = config;
		this.processor = processor;
		this.eventBuffer = eventBuffer;
	}
	
	@Override
	public void start() throws EventStreamException{
		
		Selector selector = initiateSelector();
		
		isRunning = true;
		LOG.info("Server selector ("+selector.getClass().getName()+") started on address: "+config.getAddress().toString()); 
		
		ByteBuffer buffer = createBuffer();
		
		while(isRunning){
			try{
				select(selector, buffer);
			} catch (IOException e){
				LOG.error("Error selecting event from stream", e);
			}
		}
	}
	
	private Selector initiateSelector() throws EventStreamException {
		try{
			SelectorProvider provider = SelectorProvider.provider();
			Selector socketSelector = provider.openSelector();
			
			T channel = processor.createChannel(provider);
			channel.configureBlocking(false);
			channel.bind(config.getAddress());
			
			channel.register(socketSelector, SelectionKey.OP_ACCEPT);
			
			return socketSelector;
			
		} catch (IOException e){
			throw new EventStreamException("Unable to start selector event stream", e);
		}
	}
	
	private void select(Selector selector, ByteBuffer buffer) throws IOException{
		selector.select();
		
		Set<SelectionKey> keys = selector.selectedKeys();
		
		for (SelectionKey key: keys){
			if (key.isValid() && key.isAcceptable()){
				buffer.clear();
				@SuppressWarnings("unchecked")
				T channel = (T)key.channel();
				
				ScatteringByteChannel readerChannel = processor.getByteChannel(channel);
				
				buffer.clear();
				
				while(readerChannel.read(buffer) != -1){
					buffer.flip();
					
					eventBuffer.marshal(buffer);
					
					buffer.flip();
				}
				
				Event event = eventBuffer.buildEventAndResetBuffer();
				
				@SuppressWarnings({"rawtypes" })
				Dispatcher rawDispatcher = dispatcher;
				
				rawDispatcher.dispatchEvent(event);
				
				// now close the connection
				channel.close();
				if (readerChannel.isOpen()){
					readerChannel.close();
				}
				
				key.cancel();
			}
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
		isRunning = false;
	}
	
	@Override
	public boolean isRunning(){
		return isRunning;
	}
}
