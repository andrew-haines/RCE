package com.haines.ml.rce.eventstream;

public class SelectorEventStreamConfig {

	public static enum BufferType{
		DIRECT_BUFFER,
		HEAP_BUFFER;
	}
	
	private final BufferType bufferType;
	private final int bufferCapacity;
	
	private SelectorEventStreamConfig(BufferType bufferType, int bufferCapacity){
		this.bufferType = bufferType;
		this.bufferCapacity = bufferCapacity;
	}
	
	public BufferType getBufferType(){
		return bufferType;
	}
	
	public int getBufferCapacity() {
		return bufferCapacity;
	}
	
	public static class SelectorEventStreamConfigBuilder{
		
		private BufferType bufferType = BufferType.DIRECT_BUFFER;
		private int bufferCapacity = 8192;

		public SelectorEventStreamConfigBuilder bufferType(BufferType bufferType) {
			this.bufferType = bufferType;
			
			return this;
		}
		
		public SelectorEventStreamConfigBuilder bufferCapacity(int bufferCapacity) {
			this.bufferCapacity = bufferCapacity;
			
			return this;
		}
		
		public SelectorEventStreamConfig build(){
			return new SelectorEventStreamConfig(bufferType, bufferCapacity);
		}
	}
}
