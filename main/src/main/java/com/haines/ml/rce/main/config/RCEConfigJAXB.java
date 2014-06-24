package com.haines.ml.rce.main.config;

import javax.xml.bind.annotation.XmlElement;

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
}
