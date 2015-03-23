package com.haines.ml.rce.service;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface ClassifierService {
	
	/**
	 * Returns a predicted classification given the supplied features
	 * @param features
	 * @return
	 */
	PredicatedClassification getClassification(Iterable<? extends Feature> features);

	public static class PredicatedClassification {
		
		private final double certainty;
		private final Classification classification;
		
		public PredicatedClassification(double certainty, Classification classification){
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
