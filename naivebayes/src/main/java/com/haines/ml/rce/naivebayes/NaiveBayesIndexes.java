package com.haines.ml.rce.naivebayes;

import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public abstract class NaiveBayesIndexes {
	
	public static final int NO_INDEX_FOUND = -1;

	private static final Function<? super Classification, ? extends NaiveBayesPriorProperty> CLASSIFICATION_TO_PRIOR_PROPERTY_FUNC = new Function<Classification, NaiveBayesPriorProperty>(){

		@Override
		public NaiveBayesPriorProperty apply(Classification input) {
			return new NaiveBayesPriorProperty(input);
		}
	};
	
	protected final Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes;
	protected final Map<Classification, Integer> priorProbabilityIndexes;
	protected int maxIndex;
	
	protected NaiveBayesIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, int maxIndex){
		
		this.posteriorProbabilityIndexes = checkIsEmpty(posteriorProbabilityIndexes);
		this.priorProbabilityIndexes = checkIsEmpty(priorProbabilityIndexes);
		
		this.maxIndex = maxIndex;
	}
	
	public int getPosteriorIndex(Feature feature, Classification classification){
		Map<Feature, Integer> innerMap = posteriorProbabilityIndexes.get(classification);
		
		if (innerMap != null){
			Integer index = innerMap.get(feature);
			
			if (index != null){
				return index;
			}
		}
		return NO_INDEX_FOUND;
	}
	
	public int getPriorIndex(Classification classification){
		Integer index = priorProbabilityIndexes.get(classification);
		
		if (index != null){
			return index;
		}
		return NO_INDEX_FOUND;
	}
	
	public int getMaxIndex(){
		return maxIndex;
	}
	
	protected <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
		if (!map.isEmpty()){
			throw new IllegalArgumentException("Map passed into global index must be empty");
		}
		return map;
	}
	
	Iterable<NaiveBayesPosteriorProperty> getPosteriors(){
		return new Iterable<NaiveBayesPosteriorProperty>(){

			@Override
			public Iterator<NaiveBayesPosteriorProperty> iterator() {
				
				final Iterator<Map.Entry<Classification, Map<Feature, Integer>>> keySetIt = posteriorProbabilityIndexes.entrySet().iterator();
				return new Iterator<NaiveBayesPosteriorProperty>(){

					private Iterator<Feature> currentPosteriorFeatureForClassification;
					private Classification currentClassification;
					
					@Override
					public boolean hasNext() {
						return keySetIt.hasNext() || (currentPosteriorFeatureForClassification != null && currentPosteriorFeatureForClassification.hasNext());
					}

					@Override
					public NaiveBayesPosteriorProperty next() {
						if (currentPosteriorFeatureForClassification == null || !currentPosteriorFeatureForClassification.hasNext()){
							
							Map.Entry<Classification, Map<Feature, Integer>> entry = keySetIt.next();
							
							currentClassification = entry.getKey();
							currentPosteriorFeatureForClassification = entry.getValue().keySet().iterator();
						}
						
						return new NaiveBayesPosteriorProperty(currentPosteriorFeatureForClassification.next(), currentClassification);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Backing array is immutable");
					}
					
				};
			}
			
		};
	}

	public Iterable<NaiveBayesPriorProperty> getPriors() {
		return Iterables.transform(priorProbabilityIndexes.keySet(), CLASSIFICATION_TO_PRIOR_PROPERTY_FUNC);
	}

	public void clear() {
		posteriorProbabilityIndexes.clear();
		priorProbabilityIndexes.clear();
		
		maxIndex = 0;
	}
	
	public abstract NaiveBayesIndexesProvider getGlobalIndexes();
}
