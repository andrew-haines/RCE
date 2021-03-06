package com.haines.ml.rce.naivebayes;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.service.ClassifierService;

public class NaiveBayesService implements ClassifierService {
	
	private static final Function<Entry<Double, Classification>, PredictedClassification> PREDICTED_CLASSIFICATION_FUNCTION = new Function<Entry<Double, Classification>, PredictedClassification>(){

		@Override
		public PredictedClassification apply(Entry<Double, Classification> input) {
			return new PredictedClassification(FastMath.exp(input.getKey()), input.getValue());
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
	
	public Iterable<PredictedClassification> getMaximumLikelihoodClassifications(Iterable<? extends Feature> features, int numClassifications){
		
		Map<Double, Classification> sortedClassifications = new TreeMap<Double, Classification>(INVERSE_NUMBER_COMPARATOR);
		sortedClassifications.put(Double.NEGATIVE_INFINITY, Classification.UNKNOWN);
		
		NaiveBayesProbabilities probabilities = probabilitiesProvider.getProbabilities();
		
		//System.out.println("considering classification with: "+probabilities.toString());
		
		for (Classification possibleClassification: probabilities.getAllClassifications()){
			
			double logLikelihoodProbability = getLoggedLikelihoodProbability(probabilities, features, possibleClassification);
			
			/* now add to the sorted map where sortedClassifications.values[0] is the maximum a posteriori. if we werent able to determine the likelihood probability,
			 (ie we have no examples in the training set for this classification), then ignore this class as we simply dont have enough data to go on.
			 
			*/
			
			if (!Double.isNaN(logLikelihoodProbability)){
			
				sortedClassifications.put(logLikelihoodProbability, possibleClassification);
			}
		}
		
		return Iterables.limit(Iterables.transform(sortedClassifications.entrySet(), PREDICTED_CLASSIFICATION_FUNCTION), numClassifications);
	}
	
	private final double getLoggedLikelihoodProbability(NaiveBayesProbabilities probabilities, Iterable<? extends Feature> features, Classification possibleClassification) {
		double logLikelihoodProbability = 0;
		for (Feature feature: features){
			logLikelihoodProbability += Math.log(probabilities.getPosteriorProbability(feature, possibleClassification));
		}
		logLikelihoodProbability += Math.log(probabilities.getPriorProbability(possibleClassification));
		
		return logLikelihoodProbability;
	}

	private PredictedClassification getMaximumLikelihoodClassification(Iterable<? extends Feature> features){
		return getMaximumLikelihoodClassifications(features, 1).iterator().next();
	}
	
	@Override
	public String toString(){
		return probabilitiesProvider.toString();
	}

	@Override
	public PredictedClassification getClassification(Iterable<? extends Feature> features) {
		return getMaximumLikelihoodClassification(features);
	}

	@Override
	public double getScore(Iterable<? extends Feature> features, Classification classification) {
		
		NaiveBayesProbabilities probabilities = probabilitiesProvider.getProbabilities();
		
		return FastMath.exp(getLoggedLikelihoodProbability(probabilities, features, classification));
	}
}
