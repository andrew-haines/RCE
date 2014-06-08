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

	private final NaiveBayesProbabilitiesProvider probabilitiesProvider;
	
	public NaiveBayesService(NaiveBayesProbabilitiesProvider probabilitiesProvider){
		this.probabilitiesProvider = probabilitiesProvider;
	}
	
	public Iterable<Classification> getMaximumLikelihoodClassifications(Iterable<? extends Feature> features, int numClassifications){
		
		Map<Double, Classification> sortedClassifications = new TreeMap<Double, Classification>(INVERSE_NUMBER_COMPARATOR);
		sortedClassifications.put(Double.NEGATIVE_INFINITY, Classification.UNKNOWN);
		
		NaiveBayesProbabilities probabilities = probabilitiesProvider.getProbabilities();
		
		for (Classification possibleClassification: probabilities.getAllClassifications()){
			double logLikelihoodProbability = 1;
			for (Feature feature: features){
				logLikelihoodProbability += Math.log(probabilities.getPosteriorProbability(feature, possibleClassification));
			}
			logLikelihoodProbability += Math.log(probabilities.getPriorProbability(possibleClassification));
			
			// now add to the sorted map where sortedClassifications.values[0] is the maximum a posteriori
			
			sortedClassifications.put(logLikelihoodProbability, possibleClassification);
		}
		
		return Iterables.limit(sortedClassifications.values(), numClassifications);
	}
	
	public Classification getMaximumLikelihoodClassification(Iterable<? extends Feature> features){
		return getMaximumLikelihoodClassifications(features, 1).iterator().next();
	}
}
