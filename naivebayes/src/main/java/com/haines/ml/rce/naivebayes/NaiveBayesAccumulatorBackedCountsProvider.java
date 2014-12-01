package com.haines.ml.rce.naivebayes;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public class NaiveBayesAccumulatorBackedCountsProvider implements NaiveBayesCountsProvider{

	private static final Predicate<NaiveBayesCounts<?>> NON_NULL_PREDICATE = new Predicate<NaiveBayesCounts<?>>(){

		@Override
		public boolean apply(NaiveBayesCounts<?> input) {
			return input != null;
		}
	};
	private final Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorPropertyToCountsFunction;
	private final Function<NaiveBayesPriorProperty, NaiveBayesCounts<NaiveBayesPriorProperty>> priorPropertyToCountsFunction;
	
	private final NaiveBayesIndexes indexes;
	
	public NaiveBayesAccumulatorBackedCountsProvider(final AccumulatorProvider<?> accumulator, NaiveBayesIndexes indexes){
		this.indexes = indexes;
		
		this.posteriorPropertyToCountsFunction = new Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<NaiveBayesPosteriorProperty>>(){

			@Override
			public NaiveBayesCounts<NaiveBayesPosteriorProperty> apply(NaiveBayesPosteriorProperty input) {
				
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPosteriorIndex(input.getFeature(), input.getClassification());
				
				int count = accumulator.getAccumulatorValue(slot);
				
				if (count > 0){
				
					return new NaiveBayesCounts<NaiveBayesPosteriorProperty>(input, count);
				} else{
					return null;
				}
			}
		};
		
		this.priorPropertyToCountsFunction = new Function<NaiveBayesPriorProperty, NaiveBayesCounts<NaiveBayesPriorProperty>>(){

			@Override
			public NaiveBayesCounts<NaiveBayesPriorProperty> apply(NaiveBayesPriorProperty input) {
				
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPriorIndex(input.getClassification());
				
				int count = accumulator.getAccumulatorValue(slot);
				
				if (count > 0){
					return new NaiveBayesCounts<NaiveBayesPriorProperty>(input, count);
				} else{
					return null;
				}
			}
		};
	}

	public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
		return Iterables.filter(Iterables.transform(indexes.getPosteriors(), posteriorPropertyToCountsFunction), NON_NULL_PREDICATE);
	}

	public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
		return Iterables.filter(Iterables.transform(indexes.getPriors(), priorPropertyToCountsFunction), NON_NULL_PREDICATE);
	}
	
	@Override
	public String toString(){
		return "{posteriors: "+getPosteriorCounts()+"priors: "+getPriorCounts()+"}";
	}

	@Override
	public Counts getCounts() {
		return new Counts(){

			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriors() {
				return getPriorCounts();
			}

			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriors() {
				return getPosteriorCounts();
			}
			
		};
	}
}
