package com.haines.ml.rce.accumulator.handlers;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.DistributionProvider;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

/**
 * A feature/classification handler that defines buckets for continuous variables.
 * @author haines
 *
 * @param <T>
 */
public class BucketedFeatureHandler<T extends ClassifiedEvent & Comparable<T>> implements FeatureHandler<T>, ClassificationHandler<T>{
	
	private final T[] bucketRanges;
	private final FeatureHandler<T>[] featureBuckets;
	private final ClassificationHandler<T>[] classificationBuckets;
	private final int numSlotsRequired;
	
	private <H extends FeatureHandler<T> & ClassificationHandler<T>> BucketedFeatureHandler(H[] handlers, T[] bucketRanges){
		
		this.bucketRanges = bucketRanges;
		this.featureBuckets = handlers;
		this.classificationBuckets = handlers;
		
		int numSlots = 0;
		for (H handler: handlers){
			numSlots += handler.getNumSlotsRequired();
		}
		
		this.numSlotsRequired = numSlots;
	}
	
	@Override
	public void increment(Classification classification, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup) {
		int bucketIdx = Arrays.binarySearch(bucketRanges, event);
		
		// it is unlikely that this bucket will be found so we expect a negative value representing the insertion point.
		// This will become our index into the buckets
		
		bucketIdx = FastMath.abs(bucketIdx);
		
		classificationBuckets[bucketIdx].increment(classification, event, accumulator, lookup);
	}

	@Override
	public void increment(Feature feature, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup) {
		int bucketIdx = Arrays.binarySearch(bucketRanges, event);
		
		featureBuckets[bucketIdx].increment(feature, event, accumulator, lookup);
	}

	@Override
	public DistributionProvider getDistributionProvider() {
		return null;
	}

	@Override
	public int getNumSlotsRequired() {
		return numSlotsRequired;
	}
}
