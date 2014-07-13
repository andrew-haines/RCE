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
		
		@Override
		public String toString(){
			return "typeClass: "+clazz;
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
		public PropertyType<NaiveBayesPosteriorProperty> getType() {
			return PropertyType.POSTERIOR_TYPE;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((classification == null) ? 0 : classification.hashCode());
			result = prime * result
					+ ((feature == null) ? 0 : feature.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NaiveBayesPosteriorProperty other = (NaiveBayesPosteriorProperty) obj;
			if (classification == null) {
				if (other.classification != null)
					return false;
			} else if (!classification.equals(other.classification))
				return false;
			if (feature == null) {
				if (other.feature != null)
					return false;
			} else if (!feature.equals(other.feature))
				return false;
			return true;
		}
		
		@Override
		public String toString(){
			return feature.toString() +"|"+classification.toString();
		}
	}
	
	public static class NaiveBayesPriorProperty implements NaiveBayesProperty{
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((classification == null) ? 0 : classification.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NaiveBayesPriorProperty other = (NaiveBayesPriorProperty) obj;
			if (classification == null) {
				if (other.classification != null)
					return false;
			} else if (!classification.equals(other.classification))
				return false;
			return true;
		}

		private final Classification classification;
		
		public NaiveBayesPriorProperty(Classification classification){
			this.classification = classification;
		}

		public Classification getClassification() {
			return classification;
		}

		@Override
		public PropertyType<NaiveBayesPriorProperty> getType() {
			return PropertyType.PRIOR_TYPE;
		}
		
		@Override
		public String toString(){
			return classification.toString();
		}
	}
}
