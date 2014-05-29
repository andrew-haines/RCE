package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface NaiveBayesProbabilities {

	double getPosteriorProbability(Feature feature, Classification classification);
	
	double getPriorProbability(Classification classification);
	
	Iterable<Classification> getAllClassifications();
}
