package com.haines.ml.rce.aggregator;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;

public class Aggregator {

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
		
		for (NaiveBayesCounts<? extends NaiveBayesProperty> count: counts){
			if (count.getProperty().getType() == PropertyType.POSTERIOR_TYPE){
				NaiveBayesPosteriorProperty posterior = PropertyType.POSTERIOR_TYPE.cast(count.getProperty());
				
				updatePosterior(posterior, PropertyType.POSTERIOR_TYPE.cast(count));
				
			} else if (count.getProperty().getType() == PropertyType.PRIOR_TYPE){
				NaiveBayesPriorProperty prior = PropertyType.PRIOR_TYPE.cast(count.getProperty());
				
				updatePrior(prior, PropertyType.PRIOR_TYPE.cast(count));
			}
		}
	}

	private void updatePrior(NaiveBayesPriorProperty prior, NaiveBayesCounts<NaiveBayesPriorProperty> counts) {
		createOrIncrement(priorCounts, prior.getClassification(), counts);
	}
	
	private <T, I extends NaiveBayesProperty> void createOrIncrement(Map<T, MutableNaiveBayesCounts<I>> countMap, T key, NaiveBayesCounts<I> counts){
		MutableNaiveBayesCounts<I> currentCounts = countMap.get(key);
		
		if (currentCounts == null){
			currentCounts = counts.toMutable();
			countMap.put(key, currentCounts);
		} else{
			currentCounts.addCounts(counts.getCounts());
		}
	}

	private void updatePosterior(NaiveBayesPosteriorProperty posterior, NaiveBayesCounts<NaiveBayesPosteriorProperty> counts) {
		Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>> conditionalFeatureCounts = posteriorCounts.get(posterior.getClassification());
		
		if (conditionalFeatureCounts == null){
			conditionalFeatureCounts = new THashMap<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>();
			posteriorCounts.put(posterior.getClassification(), conditionalFeatureCounts);
		}
		
		createOrIncrement(conditionalFeatureCounts, posterior.getFeature(), counts);
	}
	
	public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getAccumulatedPosteriorCounts(){
		return Iterables.concat(Iterables.transform(posteriorCounts.values(), FLATTEN_POSTERIOR_MAP_FUNC));
	}
	
	@SuppressWarnings("unchecked")
	public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getAccumulatedPriorCounts(){
		return (Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>>)(Iterable<?>)priorCounts.values();
	}
}
