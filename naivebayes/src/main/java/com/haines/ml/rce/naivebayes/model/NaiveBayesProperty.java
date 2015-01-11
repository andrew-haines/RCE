package com.haines.ml.rce.naivebayes.model;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;

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
		
		@Override
		public String toString(){
			return "typeClass: "+clazz;
		}
		
		public static final PropertyType<DiscreteNaiveBayesPosteriorProperty> DISCRETE_POSTERIOR_TYPE = new PropertyType<DiscreteNaiveBayesPosteriorProperty>(DiscreteNaiveBayesPosteriorProperty.class){};
		public static final PropertyType<NaiveBayesPosteriorDistributionProperty> DISTRIBUTION_POSTERIOR_TYPE = new PropertyType<NaiveBayesPosteriorDistributionProperty>(NaiveBayesPosteriorDistributionProperty.class){};

		
		public static final PropertyType<DiscreteNaiveBayesPriorProperty> DISCRETE_PRIOR_TYPE = new PropertyType<DiscreteNaiveBayesPriorProperty>(DiscreteNaiveBayesPriorProperty.class){};
		public static final PropertyType<NaiveBayesPriorDistributionProperty> DISTRIBUTION_PRIOR_TYPE = new PropertyType<NaiveBayesPriorDistributionProperty>(NaiveBayesPriorDistributionProperty.class){};
	}
	
	public static class NaiveBayesPriorDistributionProperty implements NaiveBayesPriorProperty {

		private final int classificationType;
		
		public NaiveBayesPriorDistributionProperty(int classificationType){
			this.classificationType = classificationType;
		}
		
		@Override
		public PropertyType<?> getType() {
			return PropertyType.DISTRIBUTION_PRIOR_TYPE;
		}

		@Override
		public final int getClassificationType() {
			return classificationType;
		}
		
		@Override
		public int hashCode(){
			return classificationType;
		}
		
		@Override
		public boolean equals(Object obj){
			if (obj instanceof NaiveBayesPriorDistributionProperty){
				
				NaiveBayesPriorDistributionProperty other = (NaiveBayesPriorDistributionProperty)obj;
				
				return this.getClassificationType() == other.getClassificationType();
			}
			
			return false;
		}
	}
	
	public static interface NaiveBayesPosteriorProperty extends NaiveBayesProperty{
		
		Classification getClassification();
		
		int getFeatureType();
	}
	
	public static interface NaiveBayesPriorProperty extends NaiveBayesProperty{
		
		int getClassificationType();
	}
	
	public static class DiscreteNaiveBayesPosteriorProperty implements NaiveBayesPosteriorProperty{
		
		private final Feature feature;
		private final Classification classification;
		
		public DiscreteNaiveBayesPosteriorProperty(Feature feature, Classification classification){
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
		public int getFeatureType() {
			return feature.getType();
		}

		@Override
		public PropertyType<DiscreteNaiveBayesPosteriorProperty> getType() {
			return PropertyType.DISCRETE_POSTERIOR_TYPE;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = prime 
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
			DiscreteNaiveBayesPosteriorProperty other = (DiscreteNaiveBayesPosteriorProperty) obj;
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
	
	public static class DiscreteNaiveBayesPriorProperty implements NaiveBayesPriorProperty{
		
		@Override
		public int hashCode() {
			final int prime = 31;
			return prime + ((classification == null) ? 0 : classification.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DiscreteNaiveBayesPriorProperty other = (DiscreteNaiveBayesPriorProperty) obj;
			if (classification == null) {
				if (other.classification != null)
					return false;
			} else if (!classification.equals(other.classification))
				return false;
			return true;
		}

		private final Classification classification;
		
		public DiscreteNaiveBayesPriorProperty(Classification classification){
			this.classification = classification;
		}

		public Classification getClassification() {
			return classification;
		}

		@Override
		public PropertyType<DiscreteNaiveBayesPriorProperty> getType() {
			return PropertyType.DISCRETE_PRIOR_TYPE;
		}
		
		@Override
		public String toString(){
			return classification.toString();
		}

		@Override
		public int getClassificationType() {
			return classification.getType();
		}
	}
}
