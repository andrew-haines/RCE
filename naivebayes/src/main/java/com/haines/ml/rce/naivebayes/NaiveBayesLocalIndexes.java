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
	private Thread currentThread = null; // used for debugging purposes.
	
	@Inject
	public NaiveBayesLocalIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes, Map<Integer, int[]> priorTypeIndexes, NaiveBayesIndexesProvider globalIndexes){
		super(posteriorProbabilityIndexes, priorProbabilityIndexes, posteriorTypeIndexes, priorTypeIndexes, globalIndexes.getIndexes().getMaxIndex());
		this.globalIndexes = globalIndexes;
	}
	
	@Inject
	public NaiveBayesLocalIndexes(@Named(INJECT_BINDING_GLOBAL_INDEXES_KEY) NaiveBayesIndexesProvider globalIndexes){
		this(new THashMap<Classification, Map<Feature, Integer>>(), new THashMap<Classification, Integer>(), new THashMap<NaiveBayesPosteriorDistributionProperty, int[]>(), new THashMap<Integer, int[]>(), globalIndexes);
	}
	
	
	@Override
	public int[] getPriorDistributionIndexes(int classificationIndex, int numIndexes) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int[] idxes = globalIndexes.getIndexes().getPriorDistributionIndexes(classificationIndex, numIndexes);
		
		if (idxes == NaiveBayesIndexes.NO_INDEXES_FOUND){
			int[] localIndexes = priorTypeIndexes.get(classificationIndex);
			
			if (localIndexes == null){
				localIndexes = new int[numIndexes];
				
				for (int i = 0; i < localIndexes.length; i++){
					localIndexes[i] = ++super.maxIndex;
				}
				priorTypeIndexes.put(classificationIndex, localIndexes);
			} else{
				checkIndexLength(null, idxes, numIndexes);
			}
			
			idxes = localIndexes;
		}
		
		return idxes;
	}

	@Override
	public int[] getPosteriorDistributionIndexes(NaiveBayesPosteriorDistributionProperty types, int numIdxes) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int[] idxes = globalIndexes.getIndexes().getPosteriorDistributionIndexes(types, numIdxes);
		
		if (idxes == NaiveBayesIndexes.NO_INDEXES_FOUND){
			int[] localIndexes = posteriorTypeIndexes.get(types);
			
			if (localIndexes == null){
				localIndexes = new int[numIdxes];
				
				for (int i = 0; i < localIndexes.length; i++){
					localIndexes[i] = ++super.maxIndex;
				}
				posteriorTypeIndexes.put(types, localIndexes);
			} else{
				checkIndexLength(types, idxes, numIdxes);
			}
			
			idxes = localIndexes;
		}
		
		return idxes;
	}

	@Override
	public int getDiscretePosteriorIndex(Feature feature, Classification classification) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int globalIndex = globalIndexes.getIndexes().getDiscretePosteriorIndex(feature, classification);
		
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
	public int getDiscretePriorIndex(Classification classification) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int globalIndex = globalIndexes.getIndexes().getDiscretePriorIndex(classification);
		
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
