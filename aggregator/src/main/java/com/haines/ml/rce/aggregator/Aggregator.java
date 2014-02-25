package com.haines.ml.rce.aggregator;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;

public class Aggregator {

	private final Map<Classification, Map<Feature, Integer>> posteriorCounts;
	private final Map<Classification, Integer> priorCounts;
	
	public Aggregator(Map<Classification, Map<Feature, Integer>> posteriorCounts, Map<Classification, Integer> priorCounts){
		this.posteriorCounts = posteriorCounts;
		this.priorCounts = priorCounts;
	}
	
	public void aggregate(Iterable<? extends NaiveBayesCounts<NaiveBayesProperty>> counts){
		
		for (NaiveBayesCounts<NaiveBayesProperty> count: counts){
			if (count.getProperty().getType() == PropertyType.POSTERIOR_TYPE){
				NaiveBayesPosteriorProperty posterior = PropertyType.POSTERIOR_TYPE.cast(count.getProperty());
				
				updatePosterior(posterior, count.getCounts());
				
			} else if (count.getProperty().getType() == PropertyType.PRIOR_TYPE){
				NaiveBayesPriorProperty prior = PropertyType.PRIOR_TYPE.cast(count.getProperty());
				
				updatePrior(prior, count.getCounts());
			}
		}
	}

	private void updatePrior(NaiveBayesPriorProperty prior, Integer counts) {
		createOrIncrement(priorCounts, prior.getClassification(), counts);
	}
	
	private <T> void createOrIncrement(Map<T, Integer> countMap, T key, Integer counts){
		Integer currentCounts = countMap.get(key);
		
		if (currentCounts == null){
			currentCounts = counts;
		} else{
			currentCounts = currentCounts + counts;
		}
		
		countMap.put(key, currentCounts);
	}

	private void updatePosterior(NaiveBayesPosteriorProperty posterior, Integer counts) {
		Map<Feature, Integer> conditionalFeatureCounts = posteriorCounts.get(posterior.getClassification());
		
		if (conditionalFeatureCounts == null){
			conditionalFeatureCounts = new THashMap<Feature, Integer>();
			posteriorCounts.put(posterior.getClassification(), conditionalFeatureCounts);
		}
		
		createOrIncrement(conditionalFeatureCounts, posterior.getFeature(), counts);
	}
}
