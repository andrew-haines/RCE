package com.haines.ml.rce.naivebayes;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public class NaiveBayesAccumulatorBackedCountsProvider implements NaiveBayesCountsProvider{

	private final Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorPropertyToCountsFunction;
	private final Function<NaiveBayesPriorProperty, NaiveBayesCounts<NaiveBayesPriorProperty>> priorPropertyToCountsFunction;
	
	private final Accumulator accumulator;
	private final NaiveBayesIndexes indexes;
	
	public NaiveBayesAccumulatorBackedCountsProvider(Accumulator accumulator, NaiveBayesIndexes indexes){
		this.accumulator = accumulator;
		this.indexes = indexes;
		
		this.posteriorPropertyToCountsFunction = new Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<NaiveBayesPosteriorProperty>>(){

			@Override
			public NaiveBayesCounts<NaiveBayesPosteriorProperty> apply(NaiveBayesPosteriorProperty input) {
				
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPosteriorIndex(input.getFeature(), input.getClassification());
				
				return new NaiveBayesCounts<NaiveBayesPosteriorProperty>(input, NaiveBayesAccumulatorBackedCountsProvider.this.accumulator.getAccumulatorValue(slot));
			}
		};
		
		this.priorPropertyToCountsFunction = new Function<NaiveBayesPriorProperty, NaiveBayesCounts<NaiveBayesPriorProperty>>(){

			@Override
			public NaiveBayesCounts<NaiveBayesPriorProperty> apply(NaiveBayesPriorProperty input) {
				
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPriorIndex(input.getClassification());

				return new NaiveBayesCounts<NaiveBayesPriorProperty>(input, NaiveBayesAccumulatorBackedCountsProvider.this.accumulator.getAccumulatorValue(slot));
			}
			
		};
	}

	@Override
	public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
		return Iterables.transform(indexes.getPostiriors(), posteriorPropertyToCountsFunction);
	}

	@Override
	public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
		return Iterables.transform(indexes.getPriors(), priorPropertyToCountsFunction);
	}

}
