package com.haines.ml.rce.window;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import javax.inject.Inject;

import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesAccumulatorBackedCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesProbability;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;

public class WindowEventConsumer<E extends ClassifiedEvent> implements EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>>>{

	private final WindowManager aggregator;
	
	@Inject
	public WindowEventConsumer(WindowManager aggregator){
		this.aggregator = aggregator;
	}
	
	@Override
	public void consume(AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>> event) {
		
		AccumulatorProvider<?> accumulatorProvider = event.getAccumulatorProvider();
		
		final RONaiveBayesMapBasedLookupStrategy<E> strategy = event.getLookupStrategy();
		
		NaiveBayesCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accumulatorProvider, strategy.getIndexes());
		
		// need to ensure that this call uses the single writer paradigm
		aggregator.addNewProvider(countsProvider, new WindowUpdatedListener() {
			
			@Override
			public void windowUpdated(NaiveBayesProbabilitiesProvider window) {
				
				//update the global indexes with a sorted version of the posterior and prior properties, sorted by most probable
				
				NaiveBayesProbabilities probabilities = window.getProbabilities();
				
				/*
				 * represents the classification/feature mapping. This does not need to be broken down to feature type as this 
				 * is simply used for sorting the indexes by the frequency of each feature value. As long as .equals and .hasCode 
				 * are implemented properly with the type, this will work
				 */
				Map<Classification, Map<Feature, Integer>> posteriors = new THashMap<Classification, Map<Feature, Integer>>(); 
				Map<Classification, Integer> priors = new THashMap<Classification, Integer>();
				
				int indexLocation = 0;
				for (NaiveBayesProbability probability: probabilities.getOrderedProbabilities()){
					if (probability.getProperty().getType() == PropertyType.POSTERIOR_TYPE){
						NaiveBayesPosteriorProperty posterior = PropertyType.POSTERIOR_TYPE.cast(probability.getProperty());
						
						Map<Feature, Integer> featureIndexMap = posteriors.get(posterior.getClassification());
						
						if (featureIndexMap == null){
							featureIndexMap = new THashMap<Feature, Integer>();
						}
						
						featureIndexMap.put(posterior.getFeature(), indexLocation++);
						
						posteriors.put(posterior.getClassification(), featureIndexMap);
						
					} else if (probability.getProperty().getType() == PropertyType.PRIOR_TYPE){
						NaiveBayesPriorProperty prior = PropertyType.PRIOR_TYPE.cast(probability.getProperty());
						
						priors.put(prior.getClassification(), indexLocation++);
					} else{
						throw new IllegalStateException("unknown naive bayes property type: "+probability.getProperty().getType());
					}
				}
				
				NaiveBayesIndexes newGlobalIndexes = new NaiveBayesGlobalIndexes(posteriors, priors);
				
				strategy.getIndexes().getGlobalIndexes().setIndexes(newGlobalIndexes);
				
				// now reset the local indexes
				
				strategy.clear();
			}
		});
		
		
	}

}
