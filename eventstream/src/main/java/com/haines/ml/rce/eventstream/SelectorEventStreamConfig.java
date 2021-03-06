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
	private final long heartBeatPeriod;
	
	private SelectorEventStreamConfig(BufferType bufferType, int bufferCapacity, SocketAddress socketAddress, ByteOrder byteOrder, long heartBeatPeriod){
		this.bufferType = bufferType;
		this.bufferCapacity = bufferCapacity;
		this.socketAddress = socketAddress;
		this.byteOrder = byteOrder;
		this.heartBeatPeriod = heartBeatPeriod;
	}
	
	public BufferType getBufferType(){
		return bufferType;
	}
	
	public ByteOrder getByteOrder(){
		return byteOrder;
	}
	
	/**
	 * Returns the milliseconds that each window represents
	 * @return
	 */
	public long getHeartBeatPeriod(){
		return heartBeatPeriod;
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
		private long heartBeatPeriod = SelectorEventStream.DO_NOT_SEND_HEART_BEAT;

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
		
		public SelectorEventStreamConfigBuilder heartBeatPeriod(long heartBeatPeriod){
			this.heartBeatPeriod = heartBeatPeriod;
			
			return this;
		}
		
		public SelectorEventStreamConfig build(){
			return new SelectorEventStreamConfig(bufferType, bufferCapacity, socketAddress, byteOrder, heartBeatPeriod);
		}
	}
}
