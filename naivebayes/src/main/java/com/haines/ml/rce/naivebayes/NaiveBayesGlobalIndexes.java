package com.haines.ml.rce.naivebayes;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.haines.ml.rce.accumulator.AccumulatorEventConsumer;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

/**
 * This class stores global indexes shared between all accumulator event consumers in a single
 * pipeline (live, staging). This is used to aid in positioning frequently used accumulators 
 * adjacent to each other so that they will probabilistically result in fewer cacheline misses. 
 * Note that the threading policy of instances of this class assumes a single writer paradim 
 * (which will be the accumulator extractor). Consequently, as the maps contained in this class
 * are not synchronised, care needs to be taken to ensure that no thread is reading values from
 * this map whilst an update is taking place. This can partially be accomplished by using a single
 * instance for the staging and live pipelines configured in {@link SwitchableAccumulatorEventConsumer}
 * but additional care will need to be taken to ensure that no existing thread is still operating
 * in a paticular pipeline prior to writing to the index
 * @author haines
 *
 */
public class NaiveBayesGlobalIndexes extends NaiveBayesIndexes{

	/**
	 * Enables constructions of this class to use a more fitting map (such as EnumMap) if
	 * feature/classification instances can be constrained.
	 * 
	 * @param posteriorProbabilityIndexes
	 * @param priorProbabilityIndexes
	 */
	public NaiveBayesGlobalIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes){
		super(posteriorProbabilityIndexes, priorProbabilityIndexes, getGreatestIndex(posteriorProbabilityIndexes, priorProbabilityIndexes)); // no index set at beginning.
	}
	
	private static int getGreatestIndex(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes) {
		Integer currentMax = NaiveBayesIndexes.NO_INDEX_FOUND;
		
		for (Map<Feature, Integer> classIndexes: posteriorProbabilityIndexes.values()){
			for (Integer idx: classIndexes.values()){
				if (currentMax < idx){
					currentMax = idx;
				}
			}
		}
		
		for (Integer idx: priorProbabilityIndexes.values()){
			if (currentMax < idx){
				currentMax = idx;
			}
		}
		
		return currentMax;
	}

	public NaiveBayesGlobalIndexes(){
		this(new THashMap<Classification, Map<Feature, Integer>>(), new THashMap<Classification, Integer>());
	}
	
	@Override
	protected <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
		return map; // global indexes can have items in them
	}

	@Override
	public void reset(AccumulatorEventConsumer<? extends ClassifiedEvent> consumer) {
		// NO OP for global indexes.
	}
}
