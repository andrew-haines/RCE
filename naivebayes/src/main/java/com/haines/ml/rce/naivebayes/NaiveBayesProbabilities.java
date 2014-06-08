package com.haines.ml.rce.naivebayes;

import java.util.Collections;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface NaiveBayesProbabilities {
	
	public static final double NOMINAL_PROBABILITY = 0.00001;
	
	public static final NaiveBayesProbabilities NOMINAL_PROBABILITIES = new NaiveBayesProbabilities(){

		@Override
		public double getPosteriorProbability(Feature feature, Classification classification) {
			return NOMINAL_PROBABILITY;
		}

		@Override
		public double getPriorProbability(Classification classification) {
			return NOMINAL_PROBABILITY;
		}

		@Override
		public Iterable<Classification> getAllClassifications() {
			return Collections.emptyList();
		}
		
	};

	double getPosteriorProbability(Feature feature, Classification classification);
	
	double getPriorProbability(Classification classification);
	
	Iterable<Classification> getAllClassifications();
}
