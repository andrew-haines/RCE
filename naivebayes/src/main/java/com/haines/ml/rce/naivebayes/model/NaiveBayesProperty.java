package com.haines.ml.rce.naivebayes.model;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface NaiveBayesProperty {

	
	public static class NaiveBayesPosteriorProperty implements NaiveBayesProperty{
		
		private final Feature feature;
		private final Classification classification;
		
		public NaiveBayesPosteriorProperty(Feature feature, Classification classification){
			this.feature = feature;
			this.classification = classification;
		}
		
		public Feature getFeature(){
			return feature;
		}
		
		public Classification getClassification(){
			return classification;
		}
	}
	
	public static class NaiveBayesPriorProperty implements NaiveBayesProperty{
		
		private final Classification classification;
		
		public NaiveBayesPriorProperty(Classification classification){
			this.classification = classification;
		}

		public Classification getClassification() {
			return classification;
		}
	}
}
