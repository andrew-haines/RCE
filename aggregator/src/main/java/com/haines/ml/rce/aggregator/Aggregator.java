package com.haines.ml.rce.aggregator;

import gnu.trove.map.hash.THashMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesAccumulatorBackedCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableDiscreteNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableNaiveBayesDistributionCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;

/**
 * This class combines a number of different count features and acts as a {@link NaiveBayesCountsProvider} that returns an aggregated
 * view of the supplied feature counts. The idea is that the feature counts will come from a mulitple cpu accumulators, implemented using instances of
 * {@link NaiveBayesAccumulatorBackedCountsProvider}. The results of which can then be fed into the WindowManager to aggregater further
 * based on time windows or directly into the {@link NaiveBayesService} for use in classification
 * @author haines
 *
 */
public class Aggregator implements NaiveBayesCountsProvider{

	private static final Function<Map<Feature, ? extends NaiveBayesCounts<?>>, Iterable<? extends NaiveBayesCounts<?>>> FLATTEN_POSTERIOR_MAP_FUNC = new Function<Map<Feature, ? extends NaiveBayesCounts<?>>, Iterable<? extends NaiveBayesCounts<?>>>(){

		@Override
		public Iterable<? extends NaiveBayesCounts<?>> apply(Map<Feature, ? extends NaiveBayesCounts<?>> input) {
			return input.values();
		}
	};
	
	/*
	 * We don't need to break out feature into feature types as this is just storing the types. As long as feature implementations
	 * have appropriate .equals and .hashcode methods that include the type parameter.
	 */
	private final MutableCounts counts;
	
	public Aggregator(Map<Classification, Map<Feature, MutableDiscreteNaiveBayesCounts>> discretePosteriorCounts, Map<Classification, MutableDiscreteNaiveBayesCounts> discretePriorCounts, Map<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts> distributionPosteriorCounts, Map<Integer, MutableNaiveBayesDistributionCounts> distributionPriorCounts){
		
		this.counts = new MutableCounts(discretePosteriorCounts, discretePriorCounts, distributionPosteriorCounts, distributionPriorCounts);
	}
	
	public void aggregate(Iterable<? extends NaiveBayesCounts<?>> counts){
		aggregate(counts, false);
	}
	
	protected void aggregate(Iterable<? extends NaiveBayesCounts<?>> counts, boolean subtract){
		
		for (NaiveBayesCounts<?> count: counts){
			PropertyType<?> type = count.getProperty().getType();
			if (type == PropertyType.DISCRETE_POSTERIOR_TYPE){
				DiscreteNaiveBayesPosteriorProperty posterior = PropertyType.DISCRETE_POSTERIOR_TYPE.cast(count.getProperty());
				
				this.updateDiscretePosterior(posterior, (MutableDiscreteNaiveBayesCounts)count.toMutable(), subtract, this.counts.discretePosteriorCounts);
				
			} else if (count.getProperty().getType() == PropertyType.DISCRETE_PRIOR_TYPE){
				DiscreteNaiveBayesPriorProperty prior = PropertyType.DISCRETE_PRIOR_TYPE.cast(count.getProperty());
				
				updateDiscretePrior(prior, (MutableDiscreteNaiveBayesCounts)count.toMutable(), subtract, this.counts.discretePriorCounts);
			} else if (count.getProperty().getType() == PropertyType.DISTRIBUTION_POSTERIOR_TYPE){
				NaiveBayesPosteriorDistributionProperty distribution = PropertyType.DISTRIBUTION_POSTERIOR_TYPE.cast(count.getProperty());
				
				updateDistributionPosterior(distribution, (MutableNaiveBayesDistributionCounts)count.toMutable(), subtract, this.counts.distributionPosteriorCounts);
				
			} else if (count.getProperty().getType() == PropertyType.DISTRIBUTION_PRIOR_TYPE){
				NaiveBayesPriorDistributionProperty distribution = PropertyType.DISTRIBUTION_PRIOR_TYPE.cast(count.getProperty());
				
				updateDistributionPrior(distribution, (MutableNaiveBayesDistributionCounts)count.toMutable(), subtract, this.counts.distributionPriorCounts);
				
			} else{
				throw new IllegalStateException("unknown naive bayes property type: "+count.getProperty().getType());
			}
		}
	}

	private void updateDistributionPrior(
			NaiveBayesPriorDistributionProperty distribution,
			MutableNaiveBayesDistributionCounts counts,
			boolean subtract,
			Map<Integer, MutableNaiveBayesDistributionCounts> distributionPriorCounts) {
		
		createOrIncrement(distributionPriorCounts, (Integer)distribution.getClassificationType(), counts, subtract);
	}

	@Override
	public String toString() {
		return "counts: "+counts.toString();
	}

	private <I extends MutableNaiveBayesCounts<I>> void updateDiscretePrior(DiscreteNaiveBayesPriorProperty prior, I counts, boolean subtract, Map<Classification, I> priorCounts) {
		createOrIncrement(priorCounts, prior.getClassification(), counts, subtract);
	}
	
	private <T, I extends MutableNaiveBayesCounts<I>> void createOrIncrement(Map<T, I> countMap, T key, I counts, boolean subtract){
		I currentCounts = countMap.get(key);
		
		if (currentCounts == null){ // if you are subtracting, there has to be an existing record to subtract from
			if (!subtract){
				currentCounts = counts.toMutable();
				countMap.put(key, currentCounts.copy());
			} else{
				throw new IllegalArgumentException("subtraction cannot occur unless there is already a record to subtract from");
			}
		} else{
			if (subtract){
				currentCounts.sub(counts);
				if (currentCounts.getCounts() == 0){ // remove reference to count object if its 0
					countMap.remove(key);
				}
			} else{
				currentCounts.add(counts);
			}
		}
	}

	private <I extends MutableNaiveBayesCounts<I>> void updateDiscretePosterior(DiscreteNaiveBayesPosteriorProperty posterior, I counts, boolean subtract, Map<Classification, Map<Feature, I>> posteriorCounts) {
		Map<Feature,I> conditionalFeatureCounts = posteriorCounts.get(posterior.getClassification());
		
		if (conditionalFeatureCounts == null){
			conditionalFeatureCounts = new THashMap<Feature, I>();
			posteriorCounts.put(posterior.getClassification(), conditionalFeatureCounts);
		}
		
		createOrIncrement(conditionalFeatureCounts, posterior.getFeature(), counts, subtract);
	}
	
	private void updateDistributionPosterior(
			NaiveBayesPosteriorDistributionProperty distribution,
			MutableNaiveBayesDistributionCounts counts,
			boolean subtract,
			Map<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts> distributionPosteriorCounts) {
		
		createOrIncrement(distributionPosteriorCounts, distribution, counts, subtract);
	}
	
	Iterable<NaiveBayesCounts<?>> getAccumulatedDiscretePosteriorCounts(){
		return getAccumulatedPosteriorCounts(counts.discretePosteriorCounts.values());
	}
	
	private static Iterable<NaiveBayesCounts<?>> getAccumulatedPosteriorCounts(Iterable<? extends Map<Feature, ? extends MutableNaiveBayesCounts<?>>> posteriorFeatureMap){
		return Iterables.concat(Iterables.transform(posteriorFeatureMap, FLATTEN_POSTERIOR_MAP_FUNC));
	}
	
	Iterable<NaiveBayesCounts<?>> getAccumulatedDiscretePriorCounts(){
		return getAccumulatedPriorCounts(this.counts.discretePriorCounts.values());
	}
	
	@SuppressWarnings("unchecked")
	private static Iterable<NaiveBayesCounts<?>> getAccumulatedPriorCounts(Iterable<? extends NaiveBayesCounts<?>> priorCounts){
		return (Iterable<NaiveBayesCounts<?>>)priorCounts;
	}
	
	public static Aggregator newInstance(){
		return new Aggregator(new ConcurrentHashMap<Classification, Map<Feature, MutableDiscreteNaiveBayesCounts>>(),
				new ConcurrentHashMap<Classification, MutableDiscreteNaiveBayesCounts>(),
				new ConcurrentHashMap<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts>(),
				new ConcurrentHashMap<Integer, MutableNaiveBayesDistributionCounts>());
	}

	@Override
	public Counts getCounts() {
		return counts;
	}
	
	private static class MutableCounts implements Counts {
		
		
		/*
		 * We don't need to break out feature into feature types as this is just storing the types. As long as feature implementations
		 * have appropriate .equals and .hashcode methods that include the type parameter.
		 */
		private final Map<Classification, Map<Feature, MutableDiscreteNaiveBayesCounts>> discretePosteriorCounts;
		private final Map<Classification, MutableDiscreteNaiveBayesCounts> discretePriorCounts;
		private final Map<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts> distributionPosteriorCounts;
		private final Map<Integer, MutableNaiveBayesDistributionCounts> distributionPriorCounts;
		
		protected MutableCounts(Map<Classification, Map<Feature, MutableDiscreteNaiveBayesCounts>> discretePosteriorCounts, Map<Classification, MutableDiscreteNaiveBayesCounts> discretePriorCounts, Map<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts> distributionPosteriorCounts, Map<Integer, MutableNaiveBayesDistributionCounts> distributionPriorCounts){
			this.discretePosteriorCounts = discretePosteriorCounts;
			this.discretePriorCounts = discretePriorCounts;
			this.distributionPosteriorCounts = distributionPosteriorCounts;
			this.distributionPriorCounts = distributionPriorCounts;
		}

		@Override
		public Iterable<NaiveBayesCounts<?>> getPriors() {
			return getAccumulatedPriorCounts(Iterables.concat(discretePriorCounts.values(), distributionPriorCounts.values()));
		}

		@Override
		public Iterable<NaiveBayesCounts<?>> getPosteriors() {
			return Iterables.concat(getAccumulatedPosteriorCounts(discretePosteriorCounts.values()), distributionPosteriorCounts.values());
		}
		
		@Override
		public String toString(){
			return "discrete priors: "+discretePriorCounts.size()+" discrete posts: "+discretePosteriorCounts.size()+ " distribution priors: "+distributionPriorCounts.size()+" distribution posts: "+distributionPosteriorCounts.size();
		}
	}
}
