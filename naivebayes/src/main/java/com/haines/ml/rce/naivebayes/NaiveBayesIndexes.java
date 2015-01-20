package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorDistributionProperty;

public interface NaiveBayesIndexes {

	public static final int NO_INDEX_FOUND = -1;
	public static final int[] NO_INDEXES_FOUND = new int[]{};
	public static final int UNKNOWN_NUM_INDEXES = -1;
	
	/**
	 * Return all the
	 * @return
	 */
	Iterable<DiscreteNaiveBayesPriorProperty> getDiscretePriors();
	
	Iterable<DiscreteNaiveBayesPosteriorProperty> getDiscretePosteriors();
	
	Iterable<NaiveBayesPosteriorDistributionProperty> getPosteriorDistributionsTypes();
	
	Iterable<NaiveBayesPriorDistributionProperty> getPriorDistributionTypes();

	int getDiscretePosteriorIndex(Feature feature, Classification classification);
	
	int[] getPosteriorDistributionIndexes(NaiveBayesPosteriorDistributionProperty types, int numIdxes);

	int getDiscretePriorIndex(Classification classification);
	
	int[] getPriorDistributionIndexes(int classificationIndex, int numIndexes);

	int getMaxIndex();

	void clear();

	NaiveBayesIndexes copy();

	NaiveBayesIndexes getGlobalIndexes();
	
	public static class NaiveBayesPosteriorDistributionProperty implements NaiveBayesPosteriorProperty{
		private final int featureType;
		private final Classification classification;
		
		public NaiveBayesPosteriorDistributionProperty(int featureType, Classification classification){
			this.featureType = featureType;
			this.classification = classification;
		}
		
		@Override
		public Classification getClassification() {
			return classification;
		}

		@Override
		public int getFeatureType() {
			return featureType;
		}
		
		@Override
		public PropertyType<?> getType() {
			return PropertyType.DISTRIBUTION_POSTERIOR_TYPE;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = prime + classification.hashCode();
			result = prime * result + featureType;
			return result;
		}

		@Override
		public boolean equals(Object obj){
			if (obj instanceof NaiveBayesPosteriorDistributionProperty){
				NaiveBayesPosteriorDistributionProperty other = (NaiveBayesPosteriorDistributionProperty)obj;
			
				return this.featureType == other.featureType && this.classification.equals(other.classification);
		
			}
			return false;
		}
		
		@Override
		public String toString(){
			return "{featureType: "+featureType+", classificationType: "+classification+"}";
		}
	}
}

