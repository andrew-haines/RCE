package com.haines.ml.rce.aggregator;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesAccumulatorBackedCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
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

	private static final Function<Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>, Iterable<? extends NaiveBayesCounts<NaiveBayesPosteriorProperty>>> FLATTEN_POSTERIOR_MAP_FUNC = new Function<Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>, Iterable<? extends NaiveBayesCounts<NaiveBayesPosteriorProperty>>>(){

		@Override
		public Iterable<? extends NaiveBayesCounts<NaiveBayesPosteriorProperty>> apply(Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>> input) {
			return input.values();
		}
	
	};
	private final Map<Classification, Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>> posteriorCounts;
	private final Map<Classification, MutableNaiveBayesCounts<NaiveBayesPriorProperty>> priorCounts;
	
	public Aggregator(Map<Classification, Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>> posteriorCounts, Map<Classification, MutableNaiveBayesCounts<NaiveBayesPriorProperty>> priorCounts){
		this.posteriorCounts = posteriorCounts;
		this.priorCounts = priorCounts;
	}
	
	public void aggregate(Iterable<? extends NaiveBayesCounts<? extends NaiveBayesProperty>> counts){
		aggregate(counts, false);
	}
	
	public void clear(){
		posteriorCounts.clear();
		priorCounts.clear();
	}
	
	protected void aggregate(Iterable<? extends NaiveBayesCounts<? extends NaiveBayesProperty>> counts, boolean subtract){
		
		for (NaiveBayesCounts<? extends NaiveBayesProperty> count: counts){
			if (count.getProperty().getType() == PropertyType.POSTERIOR_TYPE){
				NaiveBayesPosteriorProperty posterior = PropertyType.POSTERIOR_TYPE.cast(count.getProperty());
				
				updatePosterior(posterior, PropertyType.POSTERIOR_TYPE.cast(count), subtract);
				
			} else if (count.getProperty().getType() == PropertyType.PRIOR_TYPE){
				NaiveBayesPriorProperty prior = PropertyType.PRIOR_TYPE.cast(count.getProperty());
				
				updatePrior(prior, PropertyType.PRIOR_TYPE.cast(count), subtract);
			}
		}
	}

	private void updatePrior(NaiveBayesPriorProperty prior, NaiveBayesCounts<NaiveBayesPriorProperty> counts, boolean subtract) {
		createOrIncrement(priorCounts, prior.getClassification(), counts, subtract);
	}
	
	private <T, I extends NaiveBayesProperty> void createOrIncrement(Map<T, MutableNaiveBayesCounts<I>> countMap, T key, NaiveBayesCounts<I> counts, boolean subtract){
		MutableNaiveBayesCounts<I> currentCounts = countMap.get(key);
		
		if (currentCounts == null){ // if you are subtracting, there has to be an existing record to subtract from
			if (!subtract){
				currentCounts = counts.toMutable();
				countMap.put(key, currentCounts);
			} else{
				throw new IllegalArgumentException("subtraction cannot occur unless there is already a record to subtract from");
			}
		} else{
			if (subtract){
				currentCounts.subCounts(counts.getCounts());
				if (currentCounts.getCounts() == 0){ // remove reference to count object if its 0
					countMap.remove(key);
				}
			} else{
				currentCounts.addCounts(counts.getCounts());
			}
		}
	}

	private void updatePosterior(NaiveBayesPosteriorProperty posterior, NaiveBayesCounts<NaiveBayesPosteriorProperty> counts, boolean subtract) {
		Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>> conditionalFeatureCounts = posteriorCounts.get(posterior.getClassification());
		
		if (conditionalFeatureCounts == null){
			conditionalFeatureCounts = new THashMap<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>();
			posteriorCounts.put(posterior.getClassification(), conditionalFeatureCounts);
		}
		
		createOrIncrement(conditionalFeatureCounts, posterior.getFeature(), counts, subtract);
	}
	
	public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getAccumulatedPosteriorCounts(){
		return Iterables.concat(Iterables.transform(posteriorCounts.values(), FLATTEN_POSTERIOR_MAP_FUNC));
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getAccumulatedPriorCounts(){
		return (Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>>)(Iterable<?>)priorCounts.values();
	}

	@Override
	public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
		return getAccumulatedPosteriorCounts();
	}

	@Override
	public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
		return getAccumulatedPriorCounts();
	}
	
	public static Aggregator newInstance(){
		return new Aggregator(new THashMap<Classification, Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>>(),
				new THashMap<Classification, MutableNaiveBayesCounts<NaiveBayesPriorProperty>>());
	}
}
