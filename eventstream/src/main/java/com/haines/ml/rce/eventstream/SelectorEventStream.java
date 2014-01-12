package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

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
	
	private volatile boolean isAlive;
	private final SelectorEventStreamConfig config;
	private final Dispatcher<?> dispatcher;
	private final NetworkChannelProcessor<T> processor;
	private final EventBuffer<?> eventBuffer;
	private final EventStreamListener listener;
	private volatile Thread executingThread;
	
	<E extends Event> SelectorEventStream(Dispatcher<E> dispatcher, SelectorEventStreamConfig config, NetworkChannelProcessor<T> processor, EventBuffer<E> eventBuffer, EventStreamListener listener){
		this.isAlive = false;
		this.dispatcher = dispatcher;
		this.config = config;
		this.processor = processor;
		this.eventBuffer = eventBuffer;
		this.listener = listener;
	}
	
	@Override
	public void start() throws EventStreamException{
		
		executingThread = Thread.currentThread();
		ChannelDetails channelDetails = initiateSelectorAndChannel();
		
		ByteBuffer buffer = createBuffer();
		isAlive = true;
		
		listener.streamStarted();
		
		LOG.info("Server selector ("+channelDetails.getSelector().getClass().getName()+") started on address: "+config.getAddress().toString()); 
		
		while(isAlive){
			try{
				select(channelDetails.getSelector(), buffer);
				
				if (executingThread.isInterrupted()){ // we have been interrupted so stop the stream
					isAlive = false;
					executingThread = null;
				}
			} catch (IOException e){
				LOG.error("Error selecting event from stream", e);
			}
		}
		
		try {
			channelDetails.close();
		} catch (IOException e) {
			throw new EventStreamException("Unable to close selector", e);
		}
		
		listener.streamStopped();
	}
	
	private ChannelDetails initiateSelectorAndChannel() throws EventStreamException {
		try{
			SelectorProvider provider = SelectorProvider.provider();
			final Selector socketSelector = provider.openSelector();
			
			final T channel = processor.createChannel(provider);
			channel.configureBlocking(false);
			channel.bind(config.getAddress());
			
			channel.register(socketSelector, SelectionKey.OP_ACCEPT);
			
			return new ChannelDetails(socketSelector, channel);
			
		} catch (IOException e){
			throw new EventStreamException("Unable to start selector event stream", e);
		}
	}
	
	private void select(Selector selector, ByteBuffer buffer) throws IOException{
		if (selector.select() > 0){
		
			Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
			while (selectedKeys.hasNext()){
				SelectionKey key = selectedKeys.next();
				selectedKeys.remove();
				if (key.isValid()){
					if(key.isAcceptable()){
						@SuppressWarnings("unchecked")
						T channel = (T)key.channel();
						
						processor.acceptChannel(selector, channel);
					} else if (key.isReadable()){
						buffer.clear();
						
						ScatteringByteChannel readerChannel = (ScatteringByteChannel)key.channel();
						while(readerChannel.read(buffer) > 0){
							buffer.flip();
							
							eventBuffer.marshal(buffer);
							
							buffer.flip();
						}
						
						// now close the connection
						readerChannel.close();						
						
						Event event = eventBuffer.buildEventAndResetBuffer();
						
						@SuppressWarnings({"rawtypes" })
						Dispatcher rawDispatcher = dispatcher;
						
						rawDispatcher.dispatchEvent(event);
					}
				}
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
		
		if (isAlive()){
			executingThread.interrupt();
		}
	}
	
	@Override
	public boolean isAlive(){
		return isAlive;
	}
	
	private static final class ChannelDetails{
		
		private final Selector socketSelector;
		private final Channel channel;

		public ChannelDetails(Selector socketSelector, Channel channel) {
			this.socketSelector = socketSelector;
			this.channel = channel;
		}

		public Selector getSelector() {
			return socketSelector;
		}

		public void close() throws IOException {
			socketSelector.close();
			channel.close();
		}
		
		
	}
}
