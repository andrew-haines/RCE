package com.haines.ml.rce.main.config;

import java.net.SocketAddress;
import java.nio.ByteOrder;

import javax.xml.bind.annotation.XmlElement;

import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;

public class RCEConfigJAXB implements RCEConfig{

	@XmlElement
	private int numberOfEventWorkers;
	
	@XmlElement
	private StreamType eventTransportProtocol;

	@Override
	public Integer getNumberOfEventWorkers() {
		return numberOfEventWorkers;
	}

	public void setNumberOfEventWorkers(int numberOfEventWorkers) {
		this.numberOfEventWorkers = numberOfEventWorkers;
	}

	@Override
	public StreamType getEventTransportProtocal() {
		return eventTransportProtocol;
	}

	public void setEventTransportProtocal(StreamType type){
		this.eventTransportProtocol = type;
	}

	@Override
	public Integer getEventBufferCapacity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BufferType getEventBufferType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteOrder getByteOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SocketAddress getEventStreamSocketAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getFirstAccumulatorLineBitDepth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getSecondAccumulatorLineBitDepth() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getFinalAccumulatorLineBitDepth() {
		// TODO Auto-generated method stub
		return null;
	}
}
