package com.haines.ml.rce.model.system;

public interface Clock {

	long getCurrentTime();
	
	public static class SystemClock implements Clock {

		@Override
		public long getCurrentTime() {
			return System.currentTimeMillis();
		}
		
	}
	
	public static class StaticClock implements Clock {

		private final long staticTime;
		
		public StaticClock(long staticTime){
			this.staticTime = staticTime;
		}
		@Override
		public long getCurrentTime() {
			return staticTime;
		}
		
	}
}
