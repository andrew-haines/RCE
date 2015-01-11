package com.haines.ml.rce.naivebayes.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.distribution.Distribution;
import com.haines.ml.rce.model.distribution.DistributionParameters;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.model.Probability.DiscreteProbability;

public interface Probability {
	
	int getOutcomes();
	
	public static interface PosteriorProbability extends Probability{
		
		double getProbability(Feature feature, Classification classification);
	}
	
	public static interface PriorProbability extends Probability {
		
		double getProbability(Classification classification);
	}
	
	public static class DiscreteProbability implements Probability {
		
		private final int outcomes;
		private final int totals;
		private final double probability;
		
		public DiscreteProbability(int outcomes, int totals){
			this.outcomes = outcomes;
			this.totals = totals;
			this.probability = outcomes / (double)totals;
		}
		
		public double getProbability(){
			return probability;
		}

		@Override
		public int getOutcomes() {
			return outcomes;
		}

		public int getTotals() {
			return totals;
		}
		
		@Override
		public String toString(){
			return "p("+getOutcomes()+"/"+getTotals()+")";
		}
	}
	
	public static class DiscretePosteriorProbabilities implements PosteriorProbability{

		private final Map<Feature, DiscreteProbability> probabilities;
		private int totalOutcomes = 0;
		
		public DiscretePosteriorProbabilities(Map<Feature, DiscreteProbability> probabilities){
			this.probabilities = probabilities;
		}
		
		public DiscretePosteriorProbabilities(){
			this(new HashMap<Feature, DiscreteProbability>());
		}
		
		public DiscreteProbability getProbability(Feature value){
			return probabilities.get(value);
		}
		
		public void addProbability(Feature feature, DiscreteProbability probability){
			
			if (probabilities.containsKey(feature)){
				throw new IllegalStateException("There is already a discrete probability for this feature ("+feature+")");
			}
			probabilities.put(feature, probability);
			totalOutcomes += probability.getOutcomes();
		}
		
		@Override
		public int getOutcomes() {
			return totalOutcomes;
		}

		@Override
		public double getProbability(Feature feature, Classification classification) {
			
			DiscreteProbability probability = probabilities.get(feature);
			
			if (probability != null){
				return probability.getProbability();
			}
			
			return NaiveBayesProbabilities.NOMINAL_PROBABILITY;
		}
		
		public Iterable<Entry<Feature, DiscreteProbability>> getFeatureProbabilities(){
			return probabilities.entrySet();
		}
		
		@Override
		public String toString(){
			return "discrete probabilities: "+probabilities;
		}
	}
	
	public static class DiscretePriorProbabilities implements PriorProbability{

		private final Map<Classification, DiscreteProbability> probabilities;
		private int totalOutcomes = 0; 
		
		public DiscretePriorProbabilities(Map<Classification, DiscreteProbability> probabilities){
			this.probabilities = probabilities;
		}
		
		public DiscretePriorProbabilities(){
			this(new HashMap<Classification, DiscreteProbability>());
		}
		
		@Override
		public int getOutcomes() {
			return totalOutcomes;
		}

		@Override
		public double getProbability(Classification classification) {
			DiscreteProbability probability = probabilities.get(classification);
			
			if (probability != null){
				return probability.getProbability();
			}
			
			return NaiveBayesProbabilities.NOMINAL_PROBABILITY;
		}

		public void addProbability(Classification classification,DiscreteProbability discreteProbability) {
			probabilities.put(classification, discreteProbability);
			
			totalOutcomes += discreteProbability.getOutcomes();
		}
		
		public Iterable<Classification> getAllClassifications() {
			return probabilities.keySet();
		}

		public Iterable<Entry<Classification, DiscreteProbability>> getClassificationProbabilities() {
			return probabilities.entrySet();
		}
		
	}
	
	public static class DistributionProbability implements Probability, PosteriorProbability, PriorProbability{
		
		private final DistributionParameters distributionParameters;
		private final Distribution distribution;
		
		public DistributionProbability(DistributionParameters distributionParameters, Distribution distribution){
			this.distributionParameters = distributionParameters;
			this.distribution = distribution;
		}
		
		public double getProbability(double value){
			return distribution.getValue(distributionParameters, value);
		}

		@Override
		public int getOutcomes() {
			return distributionParameters.getNumSamples();
		}

		@Override
		public double getProbability(Classification classification) {
			return getProbability(((Number)classification.getValue()).doubleValue());
		}

		@Override
		public double getProbability(Feature feature, Classification classification) {
			return getProbability(((Number)feature.getValue()).doubleValue());
		}
	}
}
