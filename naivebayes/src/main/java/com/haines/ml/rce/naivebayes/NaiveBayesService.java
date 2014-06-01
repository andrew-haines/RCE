package com.haines.ml.rce.naivebayes;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class NaiveBayesService {
	
	private final Comparator<Double> INVERSE_NUMBER_COMPARATOR = new Comparator<Double>(){

		@Override
		public int compare(Double o1, Double o2) {
			return -(o1.compareTo(o2));
		}
		
	};

	private final NaiveBayesProbabilities probabilities;
	
	public NaiveBayesService(NaiveBayesProbabilities probabilities){
		this.probabilities = probabilities;
	}
	
	public Iterable<Classification> getMaximumLikelihoodClassifications(Iterable<? extends Feature> features, int numClassifications){
		
		Map<Double, Classification> sortedClassifications = new TreeMap<Double, Classification>(INVERSE_NUMBER_COMPARATOR);
		sortedClassifications.put(0D, Classification.UNKNOWN);
		
		for (Classification possibleClassification: probabilities.getAllClassifications()){
			double likelihoodProbability = 1;
			for (Feature feature: features){
				likelihoodProbability *= probabilities.getPosteriorProbability(feature, possibleClassification);
			}
			likelihoodProbability *= probabilities.getPriorProbability(possibleClassification);
			
			// now add to the sorted map where sortedClassifications.values[0] is the maximum a posteriori
			
			sortedClassifications.put(likelihoodProbability, possibleClassification);
		}
		
		return Iterables.limit(sortedClassifications.values(), numClassifications);
	}
	
	public Classification getMaximumLikelihoodClassification(Iterable<? extends Feature> features){
		return getMaximumLikelihoodClassifications(features, 1).iterator().next();
	}
}
