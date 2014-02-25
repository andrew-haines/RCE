package com.haines.ml.rce.naivebayes.model;

public class NaiveBayesCounts<T extends NaiveBayesProperty> {

	private final T property;
	private final Integer counts;
	
	public NaiveBayesCounts(T property, Integer counts){
		this.property = property;
		this.counts = counts;
	}

	public T getProperty() {
		return property;
	}

	public Integer getCounts() {
		return counts;
	}
}
