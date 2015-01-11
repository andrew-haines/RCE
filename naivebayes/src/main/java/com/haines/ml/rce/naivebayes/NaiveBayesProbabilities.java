package com.haines.ml.rce.naivebayes;

import java.util.Collections;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.Probability;

/**
 * Instances of this interface store actual posterior and prior probabilities
 * @author haines
 *
 */
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

		@Override
		public Iterable<NaiveBayesProperty> getOrderedProperties() {
			return Collections.emptyList();
		}
		
	};

	/**
	 * Returns the probability for this posterior feature/classification pair
	 * @param feature
	 * @param classification
	 * @return
	 */
	double getPosteriorProbability(Feature feature, Classification classification);
	
	/**
	 * Returns the probability for this prior classification.
	 * @param classification
	 * @return
	 */
	double getPriorProbability(Classification classification);
	
	/**
	 * Returns all the prior classifications that are stored in this container.
	 * @return
	 */
	Iterable<Classification> getAllClassifications();
	
	/**
	 * Returns the properties stored in this instance sorted by order of their {@link Probability#getOutcomes()}
	 * @return
	 */
	Iterable<NaiveBayesProperty> getOrderedProperties();
}
