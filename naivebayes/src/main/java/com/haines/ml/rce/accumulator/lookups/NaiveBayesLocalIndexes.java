package com.haines.ml.rce.accumulator.lookups;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class NaiveBayesLocalIndexes extends NaiveBayesIndexes{

	private final NaiveBayesIndexes globalIndexes;
	
	public NaiveBayesLocalIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, NaiveBayesIndexes globalIndexes){
		super(posteriorProbabilityIndexes, priorProbabilityIndexes, globalIndexes.getMaxIndex()+1);
		this.globalIndexes = globalIndexes;
	}
	
	public NaiveBayesLocalIndexes(NaiveBayesIndexes globalIndexes){
		this(new THashMap<Classification, Map<Feature, Integer>>(), new THashMap<Classification, Integer>(), globalIndexes);
	}
	@Override
	public int getPosteriorIndex(Feature feature, Classification classification) {
		int globalIndex = globalIndexes.getPosteriorIndex(feature, classification);
		
		if (globalIndex == NaiveBayesIndexes.NO_INDEX_FOUND){
			// see if we have a local index
			
			Map<Feature, Integer> innerMap = posteriorProbabilityIndexes.get(classification);
			
			if (innerMap != null){
				Integer localIndex = innerMap.get(feature);
				
				if (localIndex != null){
					return localIndex;
				}
			} else {
				innerMap = new THashMap<Feature, Integer>();
				posteriorProbabilityIndexes.put(classification, innerMap);
			}
			int newIndex = super.maxIndex++;
			innerMap.put(feature, newIndex);
			
			return newIndex;
		}
		return globalIndex;
	}

	@Override
	public int getPriorIndex(Classification classification) {
		int globalIndex = globalIndexes.getPriorIndex(classification);
		
		if (globalIndex == NaiveBayesIndexes.NO_INDEX_FOUND){
			Integer localIndex = priorProbabilityIndexes.get(classification);
			
			if (localIndex == null){
				localIndex = super.maxIndex++;
				priorProbabilityIndexes.put(classification, localIndex);
			}
			
			return localIndex;
		}
		
		return globalIndex;
	}
}
