package com.haines.ml.rce.naivebayes;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class NaiveBayesService {
	
	private static final Function<Entry<Double, Classification>, PredicatedClassification> PREDICTED_CLASSIFICATION_FUNCTION = new Function<Entry<Double, Classification>, PredicatedClassification>(){

		@Override
		public PredicatedClassification apply(Entry<Double, Classification> input) {
			return new PredicatedClassification(input.getKey(), input.getValue());
		}
		
	};
	
	private static final Comparator<Double> INVERSE_NUMBER_COMPARATOR = new Comparator<Double>(){

		@Override
		public int compare(Double o1, Double o2) {
			return -(o1.compareTo(o2));
		}
		
	};

	private final NaiveBayesProbabilitiesProvider probabilitiesProvider;
	
	public NaiveBayesService(NaiveBayesProbabilitiesProvider probabilitiesProvider){
		this.probabilitiesProvider = probabilitiesProvider;
	}
	
	public Iterable<PredicatedClassification> getMaximumLikelihoodClassifications(Iterable<? extends Feature> features, int numClassifications){
		
		Map<Double, Classification> sortedClassifications = new TreeMap<Double, Classification>(INVERSE_NUMBER_COMPARATOR);
		sortedClassifications.put(Double.NEGATIVE_INFINITY, Classification.UNKNOWN);
		
		NaiveBayesProbabilities probabilities = probabilitiesProvider.getProbabilities();
		
		for (Classification possibleClassification: probabilities.getAllClassifications()){
			double logLikelihoodProbability = 0;
			for (Feature feature: features){
				logLikelihoodProbability += Math.log(probabilities.getPosteriorProbability(feature, possibleClassification));
			}
			logLikelihoodProbability += Math.log(probabilities.getPriorProbability(possibleClassification));
			
			// now add to the sorted map where sortedClassifications.values[0] is the maximum a posteriori
			
			sortedClassifications.put(logLikelihoodProbability, possibleClassification);
		}
		
		return Iterables.limit(Iterables.transform(sortedClassifications.entrySet(), PREDICTED_CLASSIFICATION_FUNCTION), numClassifications);
	}
	
	public PredicatedClassification getMaximumLikelihoodClassification(Iterable<? extends Feature> features){
		return getMaximumLikelihoodClassifications(features, 1).iterator().next();
	}
	
	public static class PredicatedClassification {
		
		private final double certainty;
		private final Classification classification;
		
		private PredicatedClassification(double certainty, Classification classification){
			this.certainty = certainty;
			this.classification = classification;
		}

		public double getCertainty() {
			return Math.exp(certainty);
		}

		public Classification getClassification() {
			return classification;
		}
	}
}
