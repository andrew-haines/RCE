package com.haines.ml.rce.eventstream;

public interface EventStreamListener {
	
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
	
	static class NoOpEventStreamListener implements EventStreamListener{

		private NoOpEventStreamListener(){}
		
		@Override
		public void streamStarted() {}

		@Override
		public void streamStopped() {}
		
	}
}
