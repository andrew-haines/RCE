package com.haines.ml.rce.model.system;

/**
 * An abstraction that allows the system to obtain the current time so that temporal changes can be tested
 * @author haines
 *
 */
public interface Clock {
	
	public final static Clock SYSTEM_CLOCK = new SystemClock();

	long getCurrentTime();
	
	/**
	 * Returns time based on the underlying system clock
	 * @author haines
	 *
	 */
	public static class SystemClock implements Clock {

		@Override
		public long getCurrentTime() {
			return System.currentTimeMillis();
		}
	}
	
	/**
	 * Returns a deterministic static time. Used for testing
	 * @author haines
	 *
	 */
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
