package com.haines.ml.rce.main.config.jaxb;

import javax.xml.bind.annotation.XmlElement;

public class AccumulatorConfigJaxB {

	private Integer accumulatorFirstLineBitDepth;
	private Integer accumulatorSecondLineBitDepth;
	private Integer accumulatorFinalLineBitDepth;
	
	@XmlElement(name="firstLineBitDepth")
	public Integer getAccumulatorFirstLineBitDepth() {
		return accumulatorFirstLineBitDepth;
	}
	
	public void setAccumulatorFirstLineBitDepth(Integer accumulatorFirstLineBitDepth) {
		this.accumulatorFirstLineBitDepth = accumulatorFirstLineBitDepth;
	}
	
	@XmlElement(name="secondLineBitDepth")
	public Integer getAccumulatorSecondLineBitDepth() {
		return accumulatorSecondLineBitDepth;
	}
	
	public void setAccumulatorSecondLineBitDepth(Integer accumulatorSecondLineBitDepth) {
		this.accumulatorSecondLineBitDepth = accumulatorSecondLineBitDepth;
	}
	
	@XmlElement(name="finalLineBitDepth")
	public Integer getAccumulatorFinalLineBitDepth() {
		return accumulatorFinalLineBitDepth;
	}
	
	public void setAccumulatorFinalLineBitDepth(Integer accumulatorFinalLineBitDepth) {
		this.accumulatorFinalLineBitDepth = accumulatorFinalLineBitDepth;
	}
}
