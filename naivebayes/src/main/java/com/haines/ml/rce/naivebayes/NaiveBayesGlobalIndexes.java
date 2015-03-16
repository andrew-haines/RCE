package com.haines.ml.rce.naivebayes;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
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
public class NaiveBayesGlobalIndexes extends DefaultNaiveBayesIndexes{

	private static final Logger LOG = LoggerFactory.getLogger(NaiveBayesGlobalIndexes.class);
	/**
	 * Enables constructions of this class to use a more fitting map (such as EnumMap) if
	 * feature/classification instances can be constrained.
	 * 
	 * @param posteriorProbabilityIndexes
	 * @param priorProbabilityIndexes
	 */
	public NaiveBayesGlobalIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes, Map<Integer, int[]> priorTypeIndexes){
		super(posteriorProbabilityIndexes, priorProbabilityIndexes, posteriorTypeIndexes, priorTypeIndexes, getGreatestIndex(posteriorProbabilityIndexes, priorProbabilityIndexes, posteriorTypeIndexes, priorTypeIndexes)); // no index set at beginning.
	}
	
	private static int getGreatestIndex(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes, Map<Integer, int[]> priorTypeIndexes) {
		Integer currentMax = NO_INDEX_FOUND;
		
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
		
		for (int[] indexes: Iterables.concat(posteriorTypeIndexes.values(), priorTypeIndexes.values())){
			for (int idx: indexes){
				if (currentMax < idx){
					currentMax = idx;
				}
			}
		}
		
		LOG.debug("New global indexes updated with a max Idx of {}", currentMax);
		
		return currentMax;
	}

	@Inject
	public NaiveBayesGlobalIndexes(){
		this(new THashMap<Classification, Map<Feature, Integer>>(), new THashMap<Classification, Integer>(), new THashMap<NaiveBayesPosteriorDistributionProperty, int[]>(), new THashMap<Integer, int[]>());
	}
	
	@Override
	protected <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
		return map; // global indexes can have items in them
	}
	
	@Override
	public String toString(){
		return "Global Indexes";
	}

	@Override
	public NaiveBayesIndexes getGlobalIndexes() {
		return this;
	}
	
	public static class VolatileNaiveBayesGlobalIndexesProvider implements NaiveBayesIndexesProvider{

		private volatile NaiveBayesGlobalIndexes indexes;
		private Thread currentThread; // used for debugging
		
		@Inject
		public VolatileNaiveBayesGlobalIndexesProvider(NaiveBayesGlobalIndexes indexes){
			this.indexes = indexes;
		}
		
		@Override
		public NaiveBayesGlobalIndexes getIndexes() { // this can be read by multiple threads.
			return indexes;
		}

		@Override
		public void setIndexes(NaiveBayesIndexes indexes) {
			
			assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to update this index.

			this.indexes = (NaiveBayesGlobalIndexes)indexes;
		}	
	}
}
