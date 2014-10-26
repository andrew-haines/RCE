package com.haines.ml.rce.eventstream;

import java.util.Arrays;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EventStreamListener {
	
	public static final Util UTIL = new Util();
	
	public static final EventStreamListener NO_OP_LISTENER = new NoOpEventStreamListener();

	void streamStarted();
	
	void streamStopped();
	
	public static class MultipleEventStreamListener implements EventStreamListener{

		private Iterable<? extends EventStreamListener> listeners;
		
		public MultipleEventStreamListener(Iterable<? extends EventStreamListener> listeners){
			this.listeners = listeners;
		}
		
		@Override
		public void streamStarted() {
			for (EventStreamListener listener: listeners){
				listener.streamStarted();
			}
		}

		@Override
		public void streamStopped() {
			for (EventStreamListener listener: listeners){
				listener.streamStopped();
			}
		}
	}
	
	public static class Util {
		
		private Util(){}
		
		public EventStreamListener chainListeners(EventStreamListener... listeners){
			return new MultipleEventStreamListener(Arrays.asList(listeners));
		}
	}
	
	public static class NoOpEventStreamListener implements EventStreamListener{

		@Inject
		public NoOpEventStreamListener(){}
		
		@Override
		public void streamStarted() {}

		@Override
		public void streamStopped() {}
		
	}
	
	public static class SLF4JStreamListener implements EventStreamListener{
		
		private final static Logger LOG = LoggerFactory.getLogger(SLF4JStreamListener.class);

		private long timeStarted;
		
		@Override
		public void streamStarted() {
			LOG.info("Stream started");
			timeStarted = System.currentTimeMillis();
		}

		@Override
		public void streamStopped() {
			LOG.info("Stream stopped. Stream ran for "+(System.currentTimeMillis() - timeStarted)+" ms");
		}
	}
}
