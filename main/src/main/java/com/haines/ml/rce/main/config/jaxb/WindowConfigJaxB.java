package com.haines.ml.rce.main.config.jaxb;

import javax.xml.bind.annotation.XmlElement;

public class WindowConfigJaxB {

	private long windowSizeMs;
	private int numWindows;
	
	@XmlElement(name="windowSizeMs")
	public long getWindowSizeMs(){
		return windowSizeMs;
	}

	public void setWindowSizeMs(long windowSizeMs) {
		this.windowSizeMs = windowSizeMs;
	}

	@XmlElement(name="numWindows")
	public int getNumWindows() {
		return numWindows;
	}

	public void setNumWindows(int numWindows) {
		this.numWindows = numWindows;
	}
}
