package com.haines.ml.rce.naivebayes.model;

public class NaiveBayesCounts<T extends NaiveBayesProperty> {

	private final T property;
	private int counts;
	
	public NaiveBayesCounts(T property, int counts){
		this.property = property;
		if (counts == 0){
			throw new IllegalArgumentException("counts cannot be 0");
		}
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + counts;
		result = prime * result
				+ ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		NaiveBayesCounts<?> other = (NaiveBayesCounts<?>) obj;
		if (counts != other.counts)
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}

	@Override
	public String toString(){
		return "{"+getProperty().toString()+"="+getCounts()+"}";
	}


	public static class MutableNaiveBayesCounts<T extends NaiveBayesProperty> extends NaiveBayesCounts<T>{

		public MutableNaiveBayesCounts(T property, int counts) {
			super(property, counts);
		}
		
		public void addCounts(int counts){
			super.counts += counts;
		}

		public void subCounts(int counts) {
			int newCounts = super.counts - counts;
			
			if (newCounts < 0){
				throw new IllegalArgumentException("Unable to subtract counts if the results is < 0 (Ie you can only subtract if you have enough values to subtract from. Negative values are not allowed)");
			}
			super.counts = newCounts;
		}
	}
}
