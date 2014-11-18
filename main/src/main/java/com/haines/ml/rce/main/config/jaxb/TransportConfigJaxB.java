package com.haines.ml.rce.main.config.jaxb;

import javax.xml.bind.annotation.XmlElement;

public class TransportConfigJaxB {

	private String protocol;
	private int port;
	private String host;
	
	@XmlElement
	public String getProtocol(){
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@XmlElement
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@XmlElement
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
