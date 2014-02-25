package com.haines.ml.rce.accumulator.lookups;

import java.util.Map;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public abstract class NaiveBayesIndexes {
	
	static final int NO_INDEX_FOUND = -1;
	
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
	
	private <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
		if (!map.isEmpty()){
			throw new IllegalArgumentException("Map passed into global index must be empty");
		}
		return map;
	}
}
