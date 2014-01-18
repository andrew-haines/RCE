package com.haines.ml.rce.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.math.IntMath;

public class DisruptorConfig {
	
	private final int ringSize;
	
	private DisruptorConfig(int ringSize){
		this.ringSize = ringSize;
	}

	public int getRingSize() {
		return ringSize;
	}

	public static class Builder {
		
		private static final Logger LOG = LoggerFactory.getLogger(Builder.class);
		
		private static final int DEFAULT_RING_SIZE = 1*1024;
		
		private int ringSize = DEFAULT_RING_SIZE;
		
		public Builder ringSize(int ringSize){
			
			this.ringSize = ringSize;
			
			return this;
		}
		
		public DisruptorConfig build(){
			
			if (!IntMath.isPowerOfTwo(ringSize)){
				LOG.warn("The disruptor ring size is not a power of 2. This is a sub optimal buffer size. Consider using a power of 2");
			}
			
			return new DisruptorConfig(ringSize);
		}
	}
	
}
