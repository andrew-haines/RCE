package com.haines.ml.rce.naivebayes;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.Probability;

public class NaiveBayesAccumulatorBackedCountsProvider implements NaiveBayesCountsProvider{

	private final Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorPropertyToCountsFunction;
	private final Function<NaiveBayesPriorProperty, NaiveBayesCounts<NaiveBayesPriorProperty>> priorPropertyToCountsFunction;
	
	private final NaiveBayesIndexes indexes;
	
	public NaiveBayesAccumulatorBackedCountsProvider(final AccumulatorProvider accumulator, NaiveBayesIndexes indexes){
		this.indexes = indexes;
		
		this.posteriorPropertyToCountsFunction = new Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<NaiveBayesPosteriorProperty>>(){

			@Override
			public NaiveBayesCounts<NaiveBayesPosteriorProperty> apply(NaiveBayesPosteriorProperty input) {
				
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPosteriorIndex(input.getFeature(), input.getClassification());
				
				return new NaiveBayesCounts<NaiveBayesPosteriorProperty>(input, accumulator.getAccumulatorValue(slot));
			}
		};
		
		this.priorPropertyToCountsFunction = new Function<NaiveBayesPriorProperty, NaiveBayesCounts<NaiveBayesPriorProperty>>(){

			@Override
			public NaiveBayesCounts<NaiveBayesPriorProperty> apply(NaiveBayesPriorProperty input) {
				
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPriorIndex(input.getClassification());

				return new NaiveBayesCounts<NaiveBayesPriorProperty>(input, accumulator.getAccumulatorValue(slot));
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
	
	private static class NaiveBayesAccumulatorBackedProbabilities implements NaiveBayesProbabilities{

		private static final double NOMINAL_PROBABILITY = 0.00001;
		
		private final Map<Classification, Map<Feature, Probability>> posteriorProbabilities;
		private final Map<Classification, Probability> priorProbabilities;
		
		private NaiveBayesAccumulatorBackedProbabilities(NaiveBayesAccumulatorBackedCountsProvider provider){
			
			Map<Classification, Integer> postertiorTotals = getPosteriorTotals(provider);
			
			int priorTotal = getPriorTotal(provider);
			
			posteriorProbabilities = new HashMap<Classification, Map<Feature, Probability>>();
			priorProbabilities = new HashMap<Classification, Probability>();
			
			for (NaiveBayesCounts<NaiveBayesPosteriorProperty> posteriors: provider.getPosteriorCounts()){
				Map<Feature, Probability> features = posteriorProbabilities.get(posteriors.getProperty().getClassification());
				
				if (features == null){
					features = new HashMap<Feature, Probability>();
					posteriorProbabilities.put(posteriors.getProperty().getClassification(), features);
				}
				
				features.put(posteriors.getProperty().getFeature(), new Probability(posteriors.getCounts(), postertiorTotals.get(posteriors.getProperty().getClassification())));
			}
			
			for (NaiveBayesCounts<NaiveBayesPriorProperty> prior: provider.getPriorCounts()){
				priorProbabilities.put(prior.getProperty().getClassification(), new Probability(prior.getCounts(), priorTotal));
			}
		}
		
		private int getPriorTotal(NaiveBayesAccumulatorBackedCountsProvider provider) {
			int priorTotal = 0;
			
			for (NaiveBayesCounts<NaiveBayesPriorProperty> prior: provider.getPriorCounts()){
				priorTotal += prior.getCounts();
			}
			
			return priorTotal;
		}

		private Map<Classification, Integer> getPosteriorTotals(NaiveBayesAccumulatorBackedCountsProvider provider) {
			Map<Classification, Integer> postertiorTotals = new HashMap<Classification, Integer>();
			
			for (NaiveBayesCounts<NaiveBayesPosteriorProperty> posteriors: provider.getPosteriorCounts()){
				
				Integer classificationTotal = postertiorTotals.get(posteriors.getProperty().getClassification());
				
				if (classificationTotal == null){
					classificationTotal = 0;
				}
				
				classificationTotal += posteriors.getCounts();
				postertiorTotals.put(posteriors.getProperty().getClassification(), classificationTotal);
			}
			
			return postertiorTotals;
		}

		@Override
		public double getPosteriorProbability(Feature feature, Classification classification) {
			Probability probability = posteriorProbabilities.get(classification).get(feature);
			
			if (probability == null){
				return NOMINAL_PROBABILITY;
			}
			
			return probability.getProbability();
		}

		@Override
		public double getPriorProbability(Classification classification) {
			Probability probability = priorProbabilities.get(classification);
			
			if (probability == null){
				return NOMINAL_PROBABILITY;
			}
			
			return probability.getProbability();
		}

		@Override
		public Iterable<Classification> getAllClassifications() {
			return priorProbabilities.keySet();
		}
	}
}
