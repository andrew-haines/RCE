package com.haines.ml.rce.accumulator.handlers;

import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.accumulator.DistributionProvider;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.distribution.Distribution;
import com.haines.ml.rce.model.distribution.DistributionParameters;

/**
 * A feature handler that defines on online maximum likelyhood estimate of the parameters for a distribution of feature values. See
 * 
 * {@link http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm"},
 * 
 * @author haines
 *
 * @param <T>
 */
public class SequentialDistributionFeatureHandler<T extends ClassifiedEvent> implements FeatureHandler<T>, ClassificationHandler<T>, DistributionProvider{

	private static final int NUM_GAUSSIAN_PARAMETERS = 3;
	private static final int NUM_SAMPLES_IDX = 0;
	private static final int MEAN_IDX = 1;
	private static final int M2_IDX = 2;
	
	@Override
	public void increment(Feature feature, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup) {
		
		double x = ((Number)feature.getValue()).doubleValue();
		
		for (Classification classification : event.getClassificationsList()){
			
			int[] slots = lookup.getPosteriorSlots(feature, classification, NUM_GAUSSIAN_PARAMETERS); 
			
			increment(x, slots, accumulator);
		}
	}
	
	private void increment(double x, int[] slots, Accumulator<?> accumulator){

		// with the slots in the accumulator now defined we now need to update them...
		
		accumulator.incrementAccumulator(slots[NUM_SAMPLES_IDX]); // increment n
		
		int meanIdx = slots[MEAN_IDX];
		
		float mean = getMean(accumulator, slots);
		
		double delta = x - mean;
		
		accumulator.sumAccumulator(meanIdx, (float)(delta / getNumSamples(accumulator, slots))); // slight loss of precision from double to float here...
		
		accumulator.sumAccumulator(slots[M2_IDX], (float)(delta * (x - getMean(accumulator, slots)))); // slight loss of precision from double to float here...
	}
	
	@Override
	public void increment(Classification classification, T event, Accumulator<?> accumulator, AccumulatorLookupStrategy<? super T> lookup) {
		double x = ((Number)classification.getValue()).doubleValue();
		
		int slots[] = lookup.getClassificationSlots(classification, NUM_GAUSSIAN_PARAMETERS);
		
		increment(x, slots, accumulator);
		
	}
	
	private final float getMean(AccumulatorProvider<?> accumulator, int[] slots){
		return accumulator.getAccumulatorValueAsFloat(slots[MEAN_IDX]);
	}

	private final int getNumSamples(AccumulatorProvider<?> accumulator, int[] slots){
		return accumulator.getAccumulatorValue(slots[NUM_SAMPLES_IDX]);
	}
	
	private final double getVariance(AccumulatorProvider<?> accumulator, int[] slots, int numSamples){
		if (numSamples != 0){
			return accumulator.getAccumulatorValueAsFloat(slots[M2_IDX]) / (numSamples -1);
		} else{
			return 0;
		}
	}

	@Override
	public <E extends Event> DistributionParameters getDistribution(AccumulatorProvider<E> accumulator, int[] slots) {
		
		int numSamples = getNumSamples(accumulator, slots);
		double mean = getMean(accumulator, slots);
		double variance = getVariance(accumulator, slots, numSamples);
		return new DistributionParameters(numSamples, mean, variance);
	}
	
	@Override
	public Distribution getDistribution() {
		return Distribution.NORMAL_DISTRIBUTION; // TODO for the moment only make this work with a normal distribution.
	}
	
	@Override
	public DistributionProvider getDistributionProvider() {
		return this;
	}
	@Override
	public int getNumSlotsRequired() {
		return NUM_GAUSSIAN_PARAMETERS;
	}
	
}
