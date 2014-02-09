package com.haines.ml.rce.accumulator.lookups;

import gnu.trove.map.hash.THashMap;

import java.util.HashMap;
import java.util.Map;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

/**
 * This class stores global indexes shared between all accumulator event consumers in a single
 * pipeline (live, staging). This is used to aid in positioning frequently used accumulators 
 * adjacent to each other so that they will probabilistically result in few cacheline misses. 
 * Note that the threading policy of instances of this class assumes a single writer paradim 
 * (which will be the accumulator extractor). Consequently, as the maps contained in this class
 * are not synchronised, care needs to be taken to ensure that no thread is reading values from
 * this map whilst an update is taking place. This can partially be accomplished by using a single
 * instance for the staging and live pipelines configured in {@link SwitchableAccumulatorEventConsumer}
 * but additional care will need to be taken to ensure that no existing thread is still operating
 * in a paticular pipeline prior to 
 * @author haines
 *
 */
public class NaiveBayesGlobalIndexes {
	
	private final Map<Feature, Map<Classification, Integer>> posteriorProbabilityIndexes;
	private final Map<Classification, Integer> priorProbabilityIndexes;
	private int maxIndex;
	
	/**
	 * Enables constructions of this class to use a more fitting map (such as EnumMap) if
	 * feature/classification instances can be constrained.
	 * 
	 * @param posteriorProbabilityIndexes
	 * @param priorProbabilityIndexes
	 */
	public NaiveBayesGlobalIndexes(Map<Feature, Map<Classification, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes){
		this.posteriorProbabilityIndexes = checkIsEmpty(posteriorProbabilityIndexes);
		this.priorProbabilityIndexes = checkIsEmpty(priorProbabilityIndexes);

		maxIndex = 0;
	}
	
	public NaiveBayesGlobalIndexes(){
		this(new THashMap<Feature, Map<Classification, Integer>>(), new THashMap<Classification, Integer>());
	}

	private <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
		if (!map.isEmpty()){
			throw new IllegalArgumentException("Map passed into global index must be empty");
		}
		return map;
	}
	
	public int getPosteriorIndex(Feature feature, Classification classification){
		Map<Classification, Integer> innerMap = posteriorProbabilityIndexes.get(feature);
		
		if (innerMap != null){
			Integer index = innerMap.get(classification);
			
			if (index != null){
				return index;
			}
		}
		return -1;
	}
	
	public int getPriorIndex(Classification classification){
		Integer index = priorProbabilityIndexes.get(classification);
		
		if (index != null){
			return index;
		}
		return -1;
	}

	public int getMaxIndex() {
		return maxIndex;
	}
	
	
}
