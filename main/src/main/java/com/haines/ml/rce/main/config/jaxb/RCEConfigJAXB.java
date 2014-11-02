package com.haines.ml.rce.main.config.jaxb;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.config.RCEConfig.StreamType;

@XmlRootElement(name="config")
public class RCEConfigJAXB implements RCEConfig{

	private static final String BIG_BYTE_ORDER = "big";

	private static final String LITTLE_BYTE_ORDER = "little";

	private int numberOfEventWorkers;
	
	private StreamType eventTransportProtocol;
	
	private Integer eventBufferCapacity;
	
	private BufferType eventBufferType;
	
	private String eventByteOrder;

	private int eventStreamPort;

	private String eventStreamHost;

	private AccumulatorConfigJaxB accumulatorConfig;
	
	private int disruptorRingSize;
	
	private long asyncPushIntervalMs;

	@Override
	@XmlElement
	public Integer getNumberOfEventWorkers() {
		return numberOfEventWorkers;
	}

	public void setNumberOfEventWorkers(int numberOfEventWorkers) {
		this.numberOfEventWorkers = numberOfEventWorkers;
	}

	@Override
	@XmlElement
	public StreamType getEventTransportProtocal() {
		return eventTransportProtocol;
	}

	public void setEventTransportProtocal(StreamType type){
		this.eventTransportProtocol = type;
	}

	@Override
	@XmlElement
	public Integer getEventBufferCapacity() {
		return eventBufferCapacity;
	}

	@Override
	@XmlElement
	public BufferType getEventBufferType() {
		return eventBufferType;
	}
	
	@Override
	@XmlElement
	public int getDisruptorRingSize() {
		return disruptorRingSize;
	}

	@Override
	public ByteOrder getByteOrder() {
		if (getEventByteOrder().equalsIgnoreCase(BIG_BYTE_ORDER)){
			return ByteOrder.BIG_ENDIAN;
		} else if (getEventByteOrder().equalsIgnoreCase(LITTLE_BYTE_ORDER)){
			return ByteOrder.LITTLE_ENDIAN;
		} else{
			throw new RuntimeException("Unknown byte order type: "+getEventByteOrder());
		}
	}

	@Override
	public SocketAddress getEventStreamSocketAddress() {
		return InetSocketAddress.createUnresolved(getEventStreamHost(), getEventStreamPort());
	}

	@Override
	public Integer getFirstAccumulatorLineBitDepth() {
		return getAccumulatorConfig().getAccumulatorFirstLineBitDepth();
	}

	@Override
	public Integer getSecondAccumulatorLineBitDepth() {
		return getAccumulatorConfig().getAccumulatorSecondLineBitDepth();
	}

	@Override
	public Integer getFinalAccumulatorLineBitDepth() {
		return getAccumulatorConfig().getAccumulatorFinalLineBitDepth();
	}

	public void setEventBufferCapacity(Integer eventBufferCapacity) {
		this.eventBufferCapacity = eventBufferCapacity;
	}

	public void setEventBufferType(BufferType eventBufferType) {
		this.eventBufferType = eventBufferType;
	}

	@XmlElement
	public String getEventByteOrder() {
		return eventByteOrder;
	}

	public void setEventByteOrder(String eventByteOrder) {
		this.eventByteOrder = eventByteOrder;
	}

	@XmlElement
	public int getEventStreamPort() {
		return eventStreamPort;
	}

	public void setEventStreamPort(int eventStreamPort) {
		this.eventStreamPort = eventStreamPort;
	}

	@XmlElement
	public String getEventStreamHost() {
		return eventStreamHost;
	}

	public void setEventStreamHost(String eventStreamHost) {
		this.eventStreamHost = eventStreamHost;
	}
	
	public void setDisruptorRingSize(int disruptorRingSize){
		this.disruptorRingSize = disruptorRingSize;
	}

	@Override
	@XmlElement(name="aysyncPushIntervalMs")
	public long getAsyncPushIntervalMs() {
		return asyncPushIntervalMs;
	}
	
	public void setAsyncPushIntervalMs(long asyncPushIntervalMs){
		this.asyncPushIntervalMs = asyncPushIntervalMs;
	}

	@XmlElement(name="accumulator")
	public AccumulatorConfigJaxB getAccumulatorConfig() {
		return accumulatorConfig;
	}

	public void setAccumulatorConfig(AccumulatorConfigJaxB accumulator) {
		this.accumulatorConfig = accumulator;
	}
}
