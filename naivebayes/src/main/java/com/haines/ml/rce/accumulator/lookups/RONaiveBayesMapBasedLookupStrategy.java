package com.haines.ml.rce.accumulator.lookups;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

public class RONaiveBayesMapBasedLookupStrategy<T extends ClassifiedEvent> implements AccumulatorLookupStrategy<T>{

	private final NaiveBayesGlobalIndexes indexes;
	
	public RONaiveBayesMapBasedLookupStrategy(NaiveBayesGlobalIndexes indexes){
		this.indexes = indexes;
	}
	
	@Override
	public int[] getSlots(T event) {
		
		// accumulator for all feature->classification pairs and then all classifications
		int[] accumulatorIndexesToUpdate = new int[event.getFeatures().size() * event.getClassifications().size() + event.getClassifications().size()];
		
		int idx = 0;
		Iterator<Feature> featureIt = event.getFeatures().iterator();
		for (; featureIt.hasNext(); idx++){
			Feature feature = featureIt.next();
			Iterator<Classification> classificationIt = event.getClassifications().iterator();
			for (; classificationIt.hasNext(); idx++){
				Classification classification = classificationIt.next();
				
				int accumulatorIdx = indexes.getPosteriorIndex(feature, classification);
				
				if (accumulatorIdx != -1){
					accumulatorIndexesToUpdate[idx] = accumulatorIdx;
				} else{
					accumulatorIndexesToUpdate[idx] = getNewAccumulatorIdx(feature, classification);
				}
			}
		}
		
		// now add the classification accumulator idxs
		
		Iterator<Classification> classificationIt = event.getClassifications().iterator();
		
		for (; classificationIt.hasNext(); idx++){
			Classification classification = classificationIt.next();
			int potentialClassificationIdx = indexes.getPriorIndex(classification);
			
			if (potentialClassificationIdx != -1){
				accumulatorIndexesToUpdate[idx] = potentialClassificationIdx;
			} else{
				accumulatorIndexesToUpdate[idx] = getNewAccumulatorIdx(classification);
			}
		}
		return accumulatorIndexesToUpdate;
	}

	private int getNewAccumulatorIdx(Classification classification) {
		// work out how we add new classifications. Need a local map that's only updated by the current thread
		
		return 0;
	}

	private int getNewAccumulatorIdx(Feature feature, Classification classification) {
		// work out how we add new feature/classifications. Need a local map that's only updated by the current thread
		return 0;
	}

	@Override
	public int getMaxIndex() {
		return indexes.getMaxIndex();
	}

}
