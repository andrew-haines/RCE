package com.haines.ml.rce.accumulator.lookups;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes;

public class RONaiveBayesMapBasedLookupStrategy<E extends ClassifiedEvent> implements AccumulatorLookupStrategy<E>{

	public static final String LOOKUP_STRATEGY_INDEXES = "com.haines.ml.rce.accumulator.lookups.indexes";
	private final NaiveBayesIndexes indexes;
	
	@Inject
	public RONaiveBayesMapBasedLookupStrategy(@Named(LOOKUP_STRATEGY_INDEXES) NaiveBayesIndexes indexes){
		this.indexes = indexes;
	}
	
	@Override
	public int[] getSlots(E event) {
		
		// accumulator for all feature->classification pairs and then all classifications
		int[] accumulatorIndexesToUpdate = new int[(event.getFeaturesList().size() * event.getClassificationsList().size()) + event.getClassificationsList().size()];
		
		int idx = 0;
		Iterator<? extends Feature> featureIt = event.getFeaturesList().iterator();
		for (; featureIt.hasNext();){
			Feature feature = featureIt.next();
			Iterator<? extends Classification> classificationIt = event.getClassificationsList().iterator();
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
		
		Iterator<? extends Classification> classificationIt = event.getClassificationsList().iterator();
		
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
	
	public NaiveBayesIndexes getIndexes(){
		return indexes;
	}

	@Override
	public int getMaxIndex() {
		return indexes.getMaxIndex();
	}

	@Override
	public AccumulatorLookupStrategy<E> copy() {
		
		return new RONaiveBayesMapBasedLookupStrategy<E>(indexes.copy());
	}

	@Override
	public void clear() {
		indexes.clear();
	}

}
