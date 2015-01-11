package com.haines.ml.rce.naivebayes.model;

import com.haines.ml.rce.model.distribution.DistributionParameters;

public interface NaiveBayesCounts<T extends NaiveBayesCounts.MutableNaiveBayesCounts<T>> {
	
	NaiveBayesProperty getProperty();
	
	int getCounts();
	
	T toMutable();
	
	public static interface MutableNaiveBayesCounts<T extends MutableNaiveBayesCounts<T>> extends NaiveBayesCounts<T>{
		
		void add(T counts);
		
		void sub(T counts);
		
		T copy();
	}

	public static class DiscreteNaiveBayesCounts implements NaiveBayesCounts<MutableDiscreteNaiveBayesCounts>{
	
		private final NaiveBayesProperty property;
		private int counts;
		
		public DiscreteNaiveBayesCounts(NaiveBayesProperty property, int counts){
			this.property = property;
			if (counts == 0){
				throw new IllegalArgumentException("counts cannot be 0");
			}
			this.counts = counts;
		}
	
		@Override
		public NaiveBayesProperty getProperty() {
			return property;
		}
	
		@Override
		public int getCounts() {
			return counts;
		}
		
		public MutableDiscreteNaiveBayesCounts toMutable(){
			return new MutableDiscreteNaiveBayesCounts(getProperty(), getCounts());
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
			if (obj instanceof DiscreteNaiveBayesCounts){
				DiscreteNaiveBayesCounts other = (DiscreteNaiveBayesCounts) obj;
				if (counts != other.counts)
					return false;
				if (property == null) {
					if (other.property != null)
						return false;
				} else if (!property.equals(other.property))
					return false;
			}
			return true;
		}
	
		@Override
		public String toString(){
			return "{"+getProperty().toString()+"="+getCounts()+"}";
		}
	}


	public static class MutableDiscreteNaiveBayesCounts extends DiscreteNaiveBayesCounts implements MutableNaiveBayesCounts<MutableDiscreteNaiveBayesCounts>{

		public MutableDiscreteNaiveBayesCounts(NaiveBayesProperty property, int counts) {
			super(property, counts);
		}
		
		@Override
		public void add(MutableDiscreteNaiveBayesCounts counts){
			super.counts += counts.getCounts();
		}

		@Override
		public void sub(MutableDiscreteNaiveBayesCounts counts) {
			int newCounts = super.counts - counts.getCounts();
			
			if (newCounts < 0){
				throw new IllegalArgumentException("Unable to subtract counts if the results is < 0 (Ie you can only subtract if you have enough values to subtract from. Negative values are not allowed)");
			}
			super.counts = newCounts;
		}
		
		public MutableDiscreteNaiveBayesCounts toMutable(){
			return this;
		}

		@Override
		public MutableDiscreteNaiveBayesCounts copy() {
			return new MutableDiscreteNaiveBayesCounts(getProperty(), getCounts());
		}
	}
	
	public static class NaiveBayesDistributionCounts implements NaiveBayesCounts<MutableNaiveBayesDistributionCounts>{

		private DistributionParameters distribution;
		private final NaiveBayesProperty property;
		
		public NaiveBayesDistributionCounts(NaiveBayesProperty property, DistributionParameters distribution) {
			
			this.distribution = distribution;
			this.property = property;
		}
		
		public DistributionParameters getDistribution(){
			return distribution;
		}
		
		@Override
		public NaiveBayesProperty getProperty() {
			return property;
		}
		
		@Override
		public int getCounts(){
			return distribution.getNumSamples();
		}
		
		public MutableNaiveBayesDistributionCounts toMutable(){
			return new MutableNaiveBayesDistributionCounts(getProperty(), getDistribution());
		}	
	}
	
	public static class MutableNaiveBayesDistributionCounts extends NaiveBayesDistributionCounts implements MutableNaiveBayesCounts<MutableNaiveBayesDistributionCounts>{

		public MutableNaiveBayesDistributionCounts(NaiveBayesProperty property, DistributionParameters distribution) {
			super(property, distribution);
		}
		
		public void add(MutableNaiveBayesDistributionCounts distribution){
			super.distribution = DistributionParameters.MATHS.add(super.distribution, distribution.getDistribution());
		}
		
		public void sub(MutableNaiveBayesDistributionCounts distribution){
			super.distribution = DistributionParameters.MATHS.sub(super.distribution, distribution.getDistribution());
		}
		
		public MutableNaiveBayesDistributionCounts toMutable(){
			return this;
		}
		
		@Override
		public MutableNaiveBayesDistributionCounts copy(){
			return new MutableNaiveBayesDistributionCounts(getProperty(), new DistributionParameters(super.distribution.getNumSamples(), super.distribution.getMean(), super.distribution.getVariance()));
		}
	}
}
