package com.haines.ml.rce.eventstream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.system.Clock;

/**
 * A nio selector event stream for processing io events to the dispatcher. This selector is
 * intended but not enforced to be run in a seperate thread and it's {@link #start()} method will
 * block until an appropriate call to {@link #stop()}
 * @author haines
 *
 */
public class SelectorEventStream<T extends SelectableChannel & NetworkChannel, E extends Event> implements EventStreamController, EventConsumer<E>{

	public static final long DO_NOT_SEND_HEART_BEAT = 0;
	
	private static final Logger LOG = LoggerFactory.getLogger(SelectorEventStream.class);
	
	private volatile boolean isAlive;
	private final SelectorEventStreamConfig config;
	private final Dispatcher<E> dispatcher;
	private final NetworkChannelProcessor<T> processor;
	private final EventMarshalBuffer<E> eventBuffer;
	private final EventStreamListener listener;
	private volatile Thread executingThread;
	private long nextHeartBeatTime;
	private final Clock clock;
	
	@Inject
	public SelectorEventStream(Clock clock, Dispatcher<E> dispatcher, SelectorEventStreamConfig config, NetworkChannelProcessor<T> processor, EventMarshalBuffer<E> eventBuffer, EventStreamListener listener){
		this.isAlive = false;
		this.dispatcher = dispatcher;
		this.config = config;
		this.processor = processor;
		this.eventBuffer = eventBuffer;
		this.listener = listener;
		this.clock = clock;
		this.nextHeartBeatTime = clock.getCurrentTime(); 
	}
	
	@Override
	public void start() throws EventStreamException{
		
		executingThread = Thread.currentThread();
		executingThread.setName("Selector Thread - started");
		ChannelDetails<T> channelDetails = initiateSelectorAndChannel();
		
		ByteBuffer buffer = createBuffer();
		isAlive = true;
		
		LOG.info("Server selector ("+channelDetails.getSelector().getClass().getName()+") started on address: "+config.getAddress().toString()); 		
		listener.streamStarted();
		
		try(Selector selector = channelDetails.getSelector()){
			while(isAlive){
			
				select(selector, buffer);
				
				if (executingThread.isInterrupted()){ // we have been interrupted so stop the stream
					isAlive = false;
					executingThread = null;
				}
			
			}
		} catch (IOException e){
			LOG.error("Error selecting event from stream", e);
		}
		
		try {
			channelDetails.close();
		} catch (IOException e) {
			throw new EventStreamException("Unable to close selector", e);
		}
		
		listener.streamStopped();
	}
	
	private ChannelDetails<T> initiateSelectorAndChannel() throws EventStreamException {
		try{
			SelectorProvider provider = SelectorProvider.provider();
			final Selector socketSelector = provider.openSelector();
			
			final T channel = processor.createChannel(provider);
			channel.configureBlocking(false);
			
			channel.register(socketSelector, processor.getRegisterOpCodes());
			processor.connect(config.getAddress(), channel);
			
			return new ChannelDetails<T>(socketSelector, channel, processor);
			
		} catch (IOException e){
			throw new EventStreamException("Unable to start selector event stream", e);
		}
	}
	
	private void select(Selector selector, ByteBuffer buffer) throws IOException{
		
		// select from the channel until the heart beat timeout. We check the selected keys and if there is no selected key and the thread
		// is not interrupted, we send the heart beat.
		
		long heartBeatPeriod = config.getHeartBeatPeriod();
		long currentTime = clock.getCurrentTime();
		
		selector.select(heartBeatPeriod); 
		
		Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
		
		if (!selectedKeys.hasNext() && !Thread.interrupted() && nextHeartBeatTime < currentTime){
			LOG.debug("Sending heart beats...");
			
			nextHeartBeatTime = currentTime + heartBeatPeriod;
			
			dispatcher.sendHeartBeat();
			return;
		}
		
		while (selectedKeys.hasNext()){
			SelectionKey key = selectedKeys.next();
			selectedKeys.remove();
			if (key.isValid()){
				
				@SuppressWarnings("unchecked")
				T channel = (T)key.channel();
				
				if(key.isAcceptable()){
					
					processor.acceptChannel(selector, channel);
				} else if (key.isReadable()){
					buffer.clear();
					
					ScatteringByteChannel readerChannel = (ScatteringByteChannel)key.channel();
				
					int totalRead = 0;
					int tmpBytesRead = 0;
					boolean enoughBuffersReadToBuildEvent = false;
					while(!enoughBuffersReadToBuildEvent && (tmpBytesRead = processor.readFromChannel(readerChannel, buffer)) > 0){
						totalRead += tmpBytesRead;
						buffer.flip();
						
						enoughBuffersReadToBuildEvent = eventBuffer.marshal(buffer);
						
						buffer.flip();
					}
					
					// now close the connection
					processor.closeAccept(readerChannel);					
					
					if (totalRead > 0){
						E event = eventBuffer.buildEventAndResetBuffer();
						
						this.consume(event);
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
		
		executingThread.setName("Selector Thread - started");
		if (isAlive()){
			executingThread.interrupt();
			isAlive = false;
		}
	}
	
	@Override
	public boolean isAlive(){
		return isAlive;
	}
	
	private static final class ChannelDetails<T extends SelectableChannel & NetworkChannel>{
		
		private final Selector socketSelector;
		private final T channel;
		private final NetworkChannelProcessor<T> processor;

		public ChannelDetails(Selector socketSelector, T channel, NetworkChannelProcessor<T> processor) {
			this.socketSelector = socketSelector;
			this.channel = channel;
			this.processor = processor;
		}

		public Selector getSelector() {
			return socketSelector;
		}

		public void close() throws IOException {
			socketSelector.close();
			channel.close();
			
			processor.close(channel);
		}
		
		
	}

	@Override
	public void consume(E event) {
		
		listener.recievedEvent(event);
		dispatcher.dispatchEvent(event);
	}
}
