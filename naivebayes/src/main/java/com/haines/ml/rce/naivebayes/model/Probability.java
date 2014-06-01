package com.haines.ml.rce.naivebayes.model;

public class Probability {

	private final int outcomes;
	private final int totals;
	private final double probability;
	
	public Probability(int outcomes, int totals){
		this.outcomes = outcomes;
		this.totals = totals;
		this.probability = outcomes / (double)totals;
	}
	
	public double getProbability(){
		return probability;
	}

	public int getOutcomes() {
		return outcomes;
	}

	public int getTotals() {
		return totals;
	}
}
