package com.haines.ml.rce.naivebayes;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.model.distribution.DistributionParameters;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.DiscreteNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.NaiveBayesDistributionCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;

public class NaiveBayesAccumulatorBackedCountsProvider implements NaiveBayesCountsProvider{

	private final Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<?>> posteriorPropertyToCountsFunction;
	private final Function<NaiveBayesPriorProperty, NaiveBayesCounts<?>> priorPropertyToCountsFunction;
	
	private final NaiveBayesIndexes indexes;
	private final HandlerRepository<?> featureHandlers;
	
	public NaiveBayesAccumulatorBackedCountsProvider(final AccumulatorProvider<?> accumulator, NaiveBayesIndexes indexes, HandlerRepository<?> featureHandlers){
		this.indexes = indexes;
		this.featureHandlers = featureHandlers;
		
		this.posteriorPropertyToCountsFunction = new Function<NaiveBayesPosteriorProperty, NaiveBayesCounts<?>>(){

			@Override
			public NaiveBayesCounts<?> apply(NaiveBayesPosteriorProperty input) {
				
				if (PropertyType.DISCRETE_POSTERIOR_TYPE.equals(input.getType())){
					return getDiscreteCounts(PropertyType.DISCRETE_POSTERIOR_TYPE.cast(input));
				} else {
					return getDistributionCounts(PropertyType.DISTRIBUTION_POSTERIOR_TYPE.cast(input));
				}
			}
			
			private NaiveBayesDistributionCounts getDistributionCounts(NaiveBayesPosteriorDistributionProperty property) {
				int[] slots = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPosteriorDistributionIndexes(property, NaiveBayesIndexes.UNKNOWN_NUM_INDEXES);
				
				FeatureHandler<?> handler = NaiveBayesAccumulatorBackedCountsProvider.this.featureHandlers.getFeatureHandler(property.getFeatureType());
				
				DistributionParameters distribution = handler.getDistributionProvider().getDistribution(accumulator, slots); // misconfigurations will throw a null pointer. This should never happen in a correctly configured setup
				
				return new NaiveBayesDistributionCounts(property, distribution);
			}

			private DiscreteNaiveBayesCounts getDiscreteCounts(DiscreteNaiveBayesPosteriorProperty property){
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getDiscretePosteriorIndex(property.getFeature(), property.getClassification());
				
				if (slot == -1){
					slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getDiscretePosteriorIndex(property.getFeature(), property.getClassification());
				}
				int count = accumulator.getAccumulatorValue(slot);
				
				if (count > 0){
				
					return new DiscreteNaiveBayesCounts(property, count);
				} else{
					return null;
				}
			}
		};
		
		this.priorPropertyToCountsFunction = new Function<NaiveBayesPriorProperty, NaiveBayesCounts<?>>(){

			@Override
			public NaiveBayesCounts<?> apply(NaiveBayesPriorProperty input) {
				if (PropertyType.DISCRETE_PRIOR_TYPE.equals(input.getType())){
					return getDiscreteCounts(PropertyType.DISCRETE_PRIOR_TYPE.cast(input));
				} else{
					return getDistributionCounts(PropertyType.DISTRIBUTION_PRIOR_TYPE.cast(input));
				}
			}
			
			private NaiveBayesDistributionCounts getDistributionCounts(NaiveBayesPriorDistributionProperty property) {
				int[] slots = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getPriorDistributionIndexes(property.getClassificationType(), NaiveBayesIndexes.UNKNOWN_NUM_INDEXES);
				
				ClassificationHandler<?> handler = NaiveBayesAccumulatorBackedCountsProvider.this.featureHandlers.getClassificationHandler(property.getClassificationType());
				
				DistributionParameters distribution = handler.getDistributionProvider().getDistribution(accumulator, slots);
				
				return new NaiveBayesDistributionCounts(property, distribution);
			}

			private DiscreteNaiveBayesCounts getDiscreteCounts(DiscreteNaiveBayesPriorProperty property){
				int slot = NaiveBayesAccumulatorBackedCountsProvider.this.indexes.getDiscretePriorIndex(property.getClassification());
				
				int count = accumulator.getAccumulatorValue(slot);
				
				if (count > 0){
					return new DiscreteNaiveBayesCounts(property, count);
				} else{
					return null;
				}
			}
		};
	}

	private Iterable<NaiveBayesCounts<?>> getPosteriorCounts(NaiveBayesIndexes indexes) {
		return Iterables.filter(Iterables.transform(Iterables.concat(indexes.getDiscretePosteriors(), indexes.getPosteriorDistributionsTypes()), posteriorPropertyToCountsFunction), Predicates.notNull());
	}

	private Iterable<NaiveBayesCounts<?>> getPriorCounts(NaiveBayesIndexes indexes) {
		return Iterables.filter(Iterables.transform(Iterables.concat(indexes.getDiscretePriors(), indexes.getPriorDistributionTypes()), priorPropertyToCountsFunction), Predicates.notNull());
	}
	
	private Iterable<NaiveBayesCounts<?>> getPosteriorCounts(){
		return Iterables.concat(getPosteriorCounts(indexes), getPosteriorCounts(indexes.getGlobalIndexes()));
	}
	
	private Iterable<NaiveBayesCounts<?>> getPriorCounts(){
		return Iterables.concat(getPriorCounts(indexes), getPriorCounts(indexes.getGlobalIndexes()));
	}
	
	@Override
	public String toString(){
		return "{posteriors: "+getPosteriorCounts()+"priors: "+getPriorCounts()+"}";
	}

	@Override
	public Counts getCounts() {
		return new Counts(){

			@Override
			public Iterable<NaiveBayesCounts<?>> getPriors() {
				return getPriorCounts();
			}

			@Override
			public Iterable<NaiveBayesCounts<?>> getPosteriors() {
				return getPosteriorCounts();
			}
			
		};
	}
}
