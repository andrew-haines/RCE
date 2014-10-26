package com.haines.ml.rce.model.system;

public interface Clock {
	
	public final static Clock SYSTEM_CLOCK = new SystemClock();

	long getCurrentTime();
	
	public static class SystemClock implements Clock {

		@Override
		public long getCurrentTime() {
			return System.currentTimeMillis();
		}
	}
	
	public static class StaticClock implements Clock {

		private long staticTime;
		
		public StaticClock(long staticTime){
			this.staticTime = staticTime;
		}
		
		@Override
		public long getCurrentTime() {
			return staticTime;
		}
		
		public void setCurrentTime(long staticTime){
			this.staticTime = staticTime;
		}
	}
}
