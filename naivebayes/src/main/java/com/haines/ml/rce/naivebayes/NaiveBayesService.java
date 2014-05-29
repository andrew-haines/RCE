package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class NaiveBayesService {

	private final NaiveBayesProbabilities probabilities;
	
	public NaiveBayesService(NaiveBayesProbabilities probabilities){
		this.probabilities = probabilities;
	}
	
	public Classification getMaximumLikelihoodClassification(Iterable<? extends Feature> features){
		
		double currentMaximumLikelinessProbability = 0;
		Classification currentMostProbablyClassification = Classification.UNKNOWN;
		
		for (Classification possibleClassification: probabilities.getAllClassifications()){
			double likelihoodProbability = 1;
			for (Feature feature: features){
				likelihoodProbability *= probabilities.getPosteriorProbability(feature, possibleClassification);
			}
			likelihoodProbability *= probabilities.getPriorProbability(possibleClassification);
			
			// now check the maximum a posteriori
			
			if (likelihoodProbability > currentMaximumLikelinessProbability){
				currentMaximumLikelinessProbability = likelihoodProbability;
				currentMostProbablyClassification = possibleClassification;
			}
		}
		
		return currentMostProbablyClassification;
	}
}
