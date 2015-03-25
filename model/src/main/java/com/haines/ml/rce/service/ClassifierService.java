package com.haines.ml.rce.service;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface ClassifierService {
	
	/**
	 * Returns a predicted classification given the supplied features
	 * @param features
	 * @return
	 */
	PredictedClassification getClassification(Iterable<? extends Feature> features);
	
	/**
	 * Returns a score that represents how well an instance with the supplied feature set would describe the supplied classification given this
	 * model.
	 * @param features
	 * @param classification
	 * @return
	 */
	double getScore(Iterable<? extends Feature> features, Classification classification);

	public static class PredictedClassification {
		
		private final double certainty;
		private final Classification classification;
		
		public PredictedClassification(double certainty, Classification classification){
			this.certainty = certainty;
			this.classification = classification;
		}

		public double getCertainty() {
			return certainty;
		}

		public Classification getClassification() {
			return classification;
		}
	}
	
	public static class RandomisedClassifierService implements ClassifierService {

		private final List<? extends Classification> possibleClassifications;
		
		public RandomisedClassifierService(List<? extends Classification> possibleClassifications){
			this.possibleClassifications = possibleClassifications;
		}
		
		@Override
		public PredictedClassification getClassification(Iterable<? extends Feature> features) {
			
			double randomNumber = FastMath.random();
			int idx = (int)(randomNumber * possibleClassifications.size());
			
			return new PredictedClassification(FastMath.random(), possibleClassifications.get(idx));
		}

		@Override
		public double getScore(Iterable<? extends Feature> features, Classification classification) {
			return FastMath.random();
		}
		
	}
}
