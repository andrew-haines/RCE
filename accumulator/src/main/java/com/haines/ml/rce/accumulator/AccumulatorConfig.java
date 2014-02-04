package com.haines.ml.rce.accumulator;

public class AccumulatorConfig {

	private final int accumulatorSlots;
	
	public AccumulatorConfig(int accumulatorSlots){
		this.accumulatorSlots = accumulatorSlots;
	}

	public int getAccumulatorLines() {
		return accumulatorSlots;
	}
}
