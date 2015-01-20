package com.haines.ml.rce.window;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.accumulator.FeatureHandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
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
import com.haines.ml.rce.naivebayes.NaiveBayesIndexesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;

public class WindowEventConsumer<E extends ClassifiedEvent> implements EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>>>{

	private final WindowManager aggregator;
	private final FeatureHandlerRepository<E> featureHandlers;
	private final WindowConfig config;
	private final NaiveBayesIndexesProvider globalIndexProvider;
	
	@Inject
	public WindowEventConsumer(WindowManager aggregator, FeatureHandlerRepository<E> featureHandlers, WindowConfig config, NaiveBayesIndexesProvider globalIndexProvider){
		this.aggregator = aggregator;
		this.featureHandlers = featureHandlers;
		this.config = config;
		this.globalIndexProvider = globalIndexProvider;
	}
	
	@Override
	public void consume(AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy<E>> event) {
		
		AccumulatorProvider<?> accumulatorProvider = event.getAccumulatorProvider();
		
		final RONaiveBayesMapBasedLookupStrategy<E> strategy = event.getLookupStrategy();
		
		NaiveBayesCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accumulatorProvider, strategy.getIndexes(), featureHandlers);
		
		// need to ensure that this call uses the single writer paradigm
		aggregator.addNewProvider(countsProvider, new WindowUpdatedListener() {
			
			@Override
			public void newWindowCreated(NaiveBayesProbabilitiesProvider window) {
				
				//update the global indexes with a sorted version of the posterior and prior properties, sorted by most probable
				
				NaiveBayesProbabilities probabilities = window.getProbabilities();
				
				/*
				 * represents the classification/feature mapping. This does not need to be broken down to feature type as this 
				 * is simply used for sorting the indexes by the frequency of each feature value. As long as .equals and .hasCode 
				 * are implemented properly with the type, this will work
				 */
				Map<Classification, Map<Feature, Integer>> discretePosteriors = new THashMap<Classification, Map<Feature, Integer>>(); 
				Map<Classification, Integer> discretePriors = new THashMap<Classification, Integer>();
				Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes = new THashMap<NaiveBayesPosteriorDistributionProperty, int[]>();
				Map<Integer, int[]> priorTypeIndexes = new THashMap<Integer, int[]>();
				
				int indexLocation = 0;
				for (NaiveBayesProperty property: Iterables.limit(probabilities.getOrderedProperties(), config.getGlobalIndexLimit())){ // limit to the global index limit
					if (property.getType() == PropertyType.DISCRETE_POSTERIOR_TYPE){
						DiscreteNaiveBayesPosteriorProperty posterior = PropertyType.DISCRETE_POSTERIOR_TYPE.cast(property);
						
						Map<Feature, Integer> featureIndexMap = discretePosteriors.get(posterior.getClassification());
						
						if (featureIndexMap == null){
							featureIndexMap = new THashMap<Feature, Integer>();
						}
						
						featureIndexMap.put(posterior.getFeature(), indexLocation++);
						
						discretePosteriors.put(posterior.getClassification(), featureIndexMap);
						
					} else if (property.getType() == PropertyType.DISCRETE_PRIOR_TYPE){
						DiscreteNaiveBayesPriorProperty prior = PropertyType.DISCRETE_PRIOR_TYPE.cast(property);
						
						discretePriors.put(prior.getClassification(), indexLocation++);
					} else if (property.getType() == PropertyType.DISTRIBUTION_POSTERIOR_TYPE){
						NaiveBayesPosteriorDistributionProperty posterior = PropertyType.DISTRIBUTION_POSTERIOR_TYPE.cast(property);
						
						FeatureHandler<E> handler = featureHandlers.getFeatureHandler(posterior.getFeatureType());
						
						assert(!posteriorTypeIndexes.containsKey(posterior));
						
						int[] slots = new int[handler.getNumSlotsRequired()];
						
						for (int i = 0;i < slots.length; i++){
							slots[i] = indexLocation++;
						}
						
						posteriorTypeIndexes.put(posterior, slots);
						
					} else if (property.getType() == PropertyType.DISTRIBUTION_PRIOR_TYPE){
						
						NaiveBayesPriorDistributionProperty prior = PropertyType.DISTRIBUTION_PRIOR_TYPE.cast(property);
						
						ClassificationHandler<E> handler = featureHandlers.getClassificationHandler(prior.getClassificationType());
						
						assert(!priorTypeIndexes.containsKey(prior.getClassificationType()));
						
						int[] slots = new int[handler.getNumSlotsRequired()];
						
						for (int i = 0;i < slots.length; i++){
							slots[i] = indexLocation++;
						}
						
						priorTypeIndexes.put(prior.getClassificationType(), slots);
						
					} else {
						throw new IllegalStateException("unknown naive bayes property type: "+property.getType());
					}
				}
				
				NaiveBayesIndexes newGlobalIndexes = new NaiveBayesGlobalIndexes(discretePosteriors, discretePriors, posteriorTypeIndexes, priorTypeIndexes);
				
				globalIndexProvider.setIndexes(newGlobalIndexes);
			}
		});
		
	}

}
