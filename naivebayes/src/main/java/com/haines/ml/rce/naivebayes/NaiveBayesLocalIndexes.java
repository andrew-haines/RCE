package com.haines.ml.rce.naivebayes;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class NaiveBayesLocalIndexes extends DefaultNaiveBayesIndexes{

	public static final String INJECT_BINDING_GLOBAL_INDEXES_KEY = "com.haines.ml.rce.naivebayes.globalIndexes";
	private final NaiveBayesIndexesProvider globalIndexesProvider;
	private NaiveBayesIndexes currentGlobalIndexes;
	private Thread currentThread = null; // used for debugging purposes.
	
	@Inject
	public NaiveBayesLocalIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes, Map<Integer, int[]> priorTypeIndexes, NaiveBayesIndexesProvider globalIndexesProvider){
		super(posteriorProbabilityIndexes, priorProbabilityIndexes, posteriorTypeIndexes, priorTypeIndexes, globalIndexesProvider.getIndexes().getMaxIndex());
		
		this.globalIndexesProvider = globalIndexesProvider;
		this.currentGlobalIndexes = globalIndexesProvider.getIndexes();
	}
	
	@Override
	public void clear() {
		super.clear();
		
		this.currentGlobalIndexes = globalIndexesProvider.getIndexes(); // update the global index. Remember that due to the chain and order of events, this means that the global index will actually be 2 windows behind.
		
		this.maxIndex = currentGlobalIndexes.getMaxIndex(); // update to the global index max when clearing so that the global name space is 0->g.maxIdx and local name space is g.maxId -> Integer.MAX_VALUE
	}

	@Inject
	public NaiveBayesLocalIndexes(@Named(INJECT_BINDING_GLOBAL_INDEXES_KEY) NaiveBayesIndexesProvider globalIndexes){
		this(new THashMap<Classification, Map<Feature, Integer>>(), new THashMap<Classification, Integer>(), new THashMap<NaiveBayesPosteriorDistributionProperty, int[]>(), new THashMap<Integer, int[]>(), globalIndexes);
	}
	
	@Override
	public int[] getPriorDistributionIndexes(int classificationIndex, int numIndexes) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int[] idxes = currentGlobalIndexes.getPriorDistributionIndexes(classificationIndex, numIndexes);
		
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
		
		int[] idxes = currentGlobalIndexes.getPosteriorDistributionIndexes(types, numIdxes);
		
		if (idxes == NaiveBayesIndexes.NO_INDEXES_FOUND){
			int[] localIndexes = posteriorTypeIndexes.get(types);
			
			if (localIndexes == null){
				localIndexes = new int[numIdxes];
				
				for (int i = 0; i < localIndexes.length; i++){
					localIndexes[i] = ++super.maxIndex;
				}
				posteriorTypeIndexes.put(types, localIndexes);
			} else{
				checkIndexLength(types, localIndexes, numIdxes);
			}
			
			idxes = localIndexes;
		}
		
		return idxes;
	}

	@Override
	public int getDiscretePosteriorIndex(Feature feature, Classification classification) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int globalIndex = currentGlobalIndexes.getDiscretePosteriorIndex(feature, classification);
		
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
			
			int	newIndex = ++super.maxIndex;
			
			innerMap.put(feature, newIndex);
			
			return newIndex;
		}
		return globalIndex;
	}

	@Override
	public int getDiscretePriorIndex(Classification classification) {
		
		assert((currentThread == null)? (currentThread = Thread.currentThread()) == Thread.currentThread(): currentThread == Thread.currentThread()); // when running with assertions on, ensure that only one thread has access to this local index cache
		
		int globalIndex = currentGlobalIndexes.getDiscretePriorIndex(classification);
		
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
	public NaiveBayesIndexes getGlobalIndexes() {
		return currentGlobalIndexes;
	}
}
