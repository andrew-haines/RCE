package com.haines.ml.rce.naivebayes.model;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public interface NaiveBayesProperty {

	PropertyType<?> getType();
	
	public static class PropertyType<T extends NaiveBayesProperty>{
		
		private final Class<T> clazz;
		
		private PropertyType(Class<T> clazz){
			this.clazz = clazz;
		}
		
		public T cast(NaiveBayesProperty property){
			return clazz.cast(property);
		}
		
		@SuppressWarnings("unchecked")
		public NaiveBayesCounts<T> cast(NaiveBayesCounts<?> counts){
			return (NaiveBayesCounts<T>) counts;
		}
		
		public static final PropertyType<NaiveBayesPosteriorProperty> POSTERIOR_TYPE = new PropertyType<NaiveBayesPosteriorProperty>(NaiveBayesPosteriorProperty.class){};
		
		public static final PropertyType<NaiveBayesPriorProperty> PRIOR_TYPE = new PropertyType<NaiveBayesPriorProperty>(NaiveBayesPriorProperty.class){};
	}
	
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

		@Override
		public PropertyType<?> getType() {
			return PropertyType.POSTERIOR_TYPE;
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

		@Override
		public PropertyType<?> getType() {
			return PropertyType.PRIOR_TYPE;
		}
	}
}
