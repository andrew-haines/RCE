package com.haines.ml.rce.accumulator.lookups;

import java.util.Iterator;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes;

public class RONaiveBayesMapBasedLookupStrategy<T extends ClassifiedEvent> implements AccumulatorLookupStrategy<T>{

	private final NaiveBayesIndexes indexes;
	
	public RONaiveBayesMapBasedLookupStrategy(NaiveBayesIndexes indexes){
		this.indexes = indexes;
	}
	
	@Override
	public int[] getSlots(T event) {
		
		// accumulator for all feature->classification pairs and then all classifications
		int[] accumulatorIndexesToUpdate = new int[(event.getFeatures().size() * event.getClassifications().size()) + event.getClassifications().size()];
		
		int idx = 0;
		Iterator<Feature> featureIt = event.getFeatures().iterator();
		for (; featureIt.hasNext();){
			Feature feature = featureIt.next();
			Iterator<Classification> classificationIt = event.getClassifications().iterator();
			for (; classificationIt.hasNext();){
				Classification classification = classificationIt.next();
				
				int accumulatorIdx = indexes.getPosteriorIndex(feature, classification);
				
				if (accumulatorIdx != NaiveBayesIndexes.NO_INDEX_FOUND){
					accumulatorIndexesToUpdate[idx++] = accumulatorIdx;
				} else{
					throw new IllegalArgumentException("unknown index for: [feature="+feature+", classification="+classification+"]");
				}
			}
		}
		
		// now add the classification accumulator idxs
		
		Iterator<Classification> classificationIt = event.getClassifications().iterator();
		
		for (; classificationIt.hasNext();){
			Classification classification = classificationIt.next();
			int potentialClassificationIdx = indexes.getPriorIndex(classification);
			
			if (potentialClassificationIdx != NaiveBayesIndexes.NO_INDEX_FOUND){
				accumulatorIndexesToUpdate[idx++] = potentialClassificationIdx;
			} else{
				throw new IllegalArgumentException("unknown index for: [classification: "+classification+"]");
			}
		}
		return accumulatorIndexesToUpdate;
	}

	@Override
	public int getMaxIndex() {
		return indexes.getMaxIndex();
	}

}
