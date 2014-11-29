package com.haines.ml.rce.naivebayes;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class NaiveBayesLocalIndexes extends DefaultNaiveBayesIndexes{

	public static final String INJECT_BINDING_GLOBAL_INDEXES_KEY = "com.haines.ml.rce.naivebayes.globalIndexes";
	private final NaiveBayesIndexesProvider globalIndexes;
	
	@Inject
	public NaiveBayesLocalIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, NaiveBayesIndexesProvider globalIndexes){
		super(posteriorProbabilityIndexes, priorProbabilityIndexes, globalIndexes.getIndexes().getMaxIndex());
		this.globalIndexes = globalIndexes;
	}
	
	@Inject
	public NaiveBayesLocalIndexes(@Named(INJECT_BINDING_GLOBAL_INDEXES_KEY) NaiveBayesIndexesProvider globalIndexes){
		this(new THashMap<Classification, Map<Feature, Integer>>(), new THashMap<Classification, Integer>(), globalIndexes);
	}
	@Override
	public int getPosteriorIndex(Feature feature, Classification classification) {
		int globalIndex = globalIndexes.getIndexes().getPosteriorIndex(feature, classification);
		
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
			int newIndex = ++super.maxIndex;
			innerMap.put(feature, newIndex);
			
			return newIndex;
		}
		return globalIndex;
	}

	@Override
	public int getPriorIndex(Classification classification) {
		int globalIndex = globalIndexes.getIndexes().getPriorIndex(classification);
		
		if (globalIndex == NaiveBayesIndexes.NO_INDEX_FOUND){
			Integer localIndex = priorProbabilityIndexes.get(classification);
			
			if (localIndex == null){
				localIndex = ++super.maxIndex;
				priorProbabilityIndexes.put(classification, localIndex);
			}
			
			return localIndex;
		}
		
		return globalIndex;
	}

	@Override
	public NaiveBayesIndexesProvider getGlobalIndexes() {
		return globalIndexes;
	}
}
