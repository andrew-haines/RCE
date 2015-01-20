package com.haines.ml.rce.accumulator.lookups;

import java.util.Iterator;

import javax.inject.Inject;
import javax.inject.Named;

import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;

public class RONaiveBayesMapBasedLookupStrategy<E extends ClassifiedEvent> implements AccumulatorLookupStrategy<E> {

	public static final String LOOKUP_STRATEGY_INDEXES = "com.haines.ml.rce.accumulator.lookups.indexes";
	private final NaiveBayesIndexes indexes;
	
	@Inject
	public RONaiveBayesMapBasedLookupStrategy(@Named(LOOKUP_STRATEGY_INDEXES) NaiveBayesIndexes indexes){
		this.indexes = indexes;
	}
	
	int[] getSlots(E event){
		int[] allSlots = new int[]{};
		
		for (Feature feature: event.getFeaturesList()){
			int[] featureSlots = this.getSlots(feature, event);
			
			allSlots = combine(allSlots, featureSlots);
		}
		
		int i = 0;
		
		int[] priorSlots = new int[event.getClassificationsList().size()];
		for (Classification classification: event.getClassificationsList()){
			int classificationSlot = this.getSlot(classification, event);
			
			priorSlots[i++] = classificationSlot;
		}
		allSlots = combine(allSlots, priorSlots);
		
		
		return allSlots;
	}
	
	private final int[] combine(int[] array1, int[] array2){
		int[] tmp = new int[array1.length + array2.length];
		
		System.arraycopy(array1, 0, tmp, 0, array1.length);
		System.arraycopy(array2, 0, tmp, array1.length, array2.length);
		
		return tmp;
	}
	
	@Override
	public int[] getSlots(Feature feature, E event) {
		
		// accumulator for all feature->classification pairs and then all classifications
		int[] accumulatorIndexesToUpdate = new int[event.getClassificationsList().size()];
		
		int idx = 0;

		Iterator<? extends Classification> classificationIt = event.getClassificationsList().iterator();
		while (classificationIt.hasNext()){
			Classification classification = classificationIt.next();
			
			accumulatorIndexesToUpdate[idx++] = getPosteriorIndex(feature, classification);
		}
		
		return accumulatorIndexesToUpdate;
	}
	
	@Override
	public int getSlot(Classification classification, E event) {
		return getClassificationSlot(classification);
	}
	
	private final int getPosteriorIndex(Feature feature, Classification classification){
		int accumulatorIdx = indexes.getDiscretePosteriorIndex(feature, classification);
		
		if (accumulatorIdx != NaiveBayesIndexes.NO_INDEX_FOUND){
			 return accumulatorIdx;
		} else{
			throw new IllegalArgumentException("unknown index for: [feature="+feature+", classification="+classification+"]");
		}
	}
	
	private final int getPriorIndex(Classification classification){
		int potentialClassificationIdx = indexes.getDiscretePriorIndex(classification);
		
		if (potentialClassificationIdx != NaiveBayesIndexes.NO_INDEX_FOUND){
			return potentialClassificationIdx;
		} else{
			throw new IllegalArgumentException("unknown index for: [classification: "+classification+"]");
		}
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

	@Override
	public final int[] getClassificationSlots(Classification classification, int numSlots) {
		int[] idxes = indexes.getPriorDistributionIndexes(classification.getType(), numSlots);
		
		if (idxes != NaiveBayesIndexes.NO_INDEXES_FOUND){
			return idxes;
		} else{
			throw new IllegalArgumentException("unknown index for [classification: "+classification+"]");
		}
	}

	@Override
	public final int[] getPosteriorSlots(Feature feature,Classification classification, int numSlots) {
		return indexes.getPosteriorDistributionIndexes(new NaiveBayesPosteriorDistributionProperty(feature.getType(), classification), numSlots);
	}

	@Override
	public final int getClassificationSlot(Classification classification) {
		return getPriorIndex(classification);
	}
}
