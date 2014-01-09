package com.haines.ml.rce.eventstream;

import java.net.SocketAddress;
import java.nio.ByteOrder;

public class SelectorEventStreamConfig {

	public static enum BufferType{
		DIRECT_BUFFER,
		HEAP_BUFFER;
	}
	
	private final BufferType bufferType;
	private final int bufferCapacity;
	private final SocketAddress socketAddress;
	private final ByteOrder byteOrder;
	
	private SelectorEventStreamConfig(BufferType bufferType, int bufferCapacity, SocketAddress socketAddress, ByteOrder byteOrder){
		this.bufferType = bufferType;
		this.bufferCapacity = bufferCapacity;
		this.socketAddress = socketAddress;
		this.byteOrder = byteOrder;
	}
	
	public BufferType getBufferType(){
		return bufferType;
	}
	
	public ByteOrder getByteOrder(){
		return byteOrder;
	}
	
	public int getBufferCapacity() {
		return bufferCapacity;
	}
	
	public SocketAddress getAddress() {
		return socketAddress;
	}
	
	public static class SelectorEventStreamConfigBuilder{
		
		private BufferType bufferType = BufferType.DIRECT_BUFFER;
		private int bufferCapacity = 8192;
		private SocketAddress socketAddress;
		private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;

		public SelectorEventStreamConfigBuilder bufferType(BufferType bufferType) {
			this.bufferType = bufferType;
			
			return this;
		}
		
		public SelectorEventStreamConfigBuilder bufferCapacity(int bufferCapacity) {
			this.bufferCapacity = bufferCapacity;
			
			return this;
		}
		
		public SelectorEventStreamConfigBuilder socketAddress(SocketAddress socketAddress) {
			this.socketAddress = socketAddress;
			
			return this;
		}
		
		public SelectorEventStreamConfigBuilder byteOrder(ByteOrder byteOrder){
			this.byteOrder = byteOrder;
			
			return this;
		}
		
		public SelectorEventStreamConfig build(){
			return new SelectorEventStreamConfig(bufferType, bufferCapacity, socketAddress, byteOrder);
		}
	}
}
