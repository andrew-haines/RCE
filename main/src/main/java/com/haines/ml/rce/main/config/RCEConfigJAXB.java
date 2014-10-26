package com.haines.ml.rce.main.config;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;

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

	private Integer accumulatorFirstLineBitDepth;

	private Integer accumulatorSecondLineBitDepth;

	private Integer accumulatorFinalLineBitDepth;
	
	private int disruptorRingSize;

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
		return getAccumulatorFirstLineBitDepth();
	}

	@Override
	public Integer getSecondAccumulatorLineBitDepth() {
		return getAccumulatorSecondLineBitDepth();
	}

	@Override
	public Integer getFinalAccumulatorLineBitDepth() {
		return getAccumulatorFinalLineBitDepth();
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

	@XmlElement
	public Integer getAccumulatorFirstLineBitDepth() {
		return accumulatorFirstLineBitDepth;
	}

	public void setAccumulatorFirstLineBitDepth(Integer accumulatorFirstLineBitDepth) {
		this.accumulatorFirstLineBitDepth = accumulatorFirstLineBitDepth;
	}

	@XmlElement
	public Integer getAccumulatorSecondLineBitDepth() {
		return accumulatorSecondLineBitDepth;
	}

	public void setAccumulatorSecondLineBitDepth(Integer accumulatorSecondLineBitDepth) {
		this.accumulatorSecondLineBitDepth = accumulatorSecondLineBitDepth;
	}

	@XmlElement
	public Integer getAccumulatorFinalLineBitDepth() {
		return accumulatorFinalLineBitDepth;
	}

	public void setAccumulatorFinalLineBitDepth(Integer accumulatorFinalLineBitDepth) {
		this.accumulatorFinalLineBitDepth = accumulatorFinalLineBitDepth;
	}
	
	public void setDisruptorRingSize(int disruptorRingSize){
		this.disruptorRingSize = disruptorRingSize;
	}
}
