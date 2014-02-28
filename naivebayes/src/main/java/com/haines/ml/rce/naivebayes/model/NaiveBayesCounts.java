package com.haines.ml.rce.naivebayes.model;

public class NaiveBayesCounts<T extends NaiveBayesProperty> {

	private final T property;
	private int counts;
	
	public NaiveBayesCounts(T property, int counts){
		this.property = property;
		this.counts = counts;
	}

	public T getProperty() {
		return property;
	}

	public int getCounts() {
		return counts;
	}
	
	public MutableNaiveBayesCounts<T> toMutable(){
		return new MutableNaiveBayesCounts<T>(getProperty(), getCounts());
	}
	
	public static class MutableNaiveBayesCounts<T extends NaiveBayesProperty> extends NaiveBayesCounts<T>{

		public MutableNaiveBayesCounts(T property, int counts) {
			super(property, counts);
		}
		
		public void addCounts(int counts){
			super.counts += counts;
		}
	}
}
