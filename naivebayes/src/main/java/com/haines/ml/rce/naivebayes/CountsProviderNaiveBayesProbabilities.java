package com.haines.ml.rce.naivebayes;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.haines.ml.rce.accumulator.DistributionProvider;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider.Counts;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.NaiveBayesDistributionCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.PropertyType;
import com.haines.ml.rce.naivebayes.model.Probability;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.Probability.DiscretePosteriorProbabilities;
import com.haines.ml.rce.naivebayes.model.Probability.DiscretePriorProbabilities;
import com.haines.ml.rce.naivebayes.model.Probability.DiscreteProbability;
import com.haines.ml.rce.naivebayes.model.Probability.DistributionProbability;
import com.haines.ml.rce.naivebayes.model.Probability.PosteriorProbability;
import com.haines.ml.rce.naivebayes.model.Probability.PriorProbability;

/**
 * This class is responsible for converting an iteration of {@link NaiveBayesCounts} object provided by an instance of {@link NaiveBayesCountsProvider}
 * and calculating their probabilities into {@link Probability} objects. Note that this also handles the translation of {@link DiscreteNaiveBayesCounts}
 * into {@link DiscreteProbability} and {@link NaiveBayesDistributionCounts} into {@link DistributionProbability} instances.
 * @author haines
 *
 */
public class CountsProviderNaiveBayesProbabilities implements NaiveBayesProbabilities{
	
	private static final Comparator<? super NaiveBayesProbability> MOST_PROPULAR_OUTCOMES_PROBABILITIES_COMPARATOR = new Comparator<NaiveBayesProbability>(){

		@Override
		public int compare(NaiveBayesProbability o1, NaiveBayesProbability o2) {
			return o2.getProbability().getOutcomes() - o1.getProbability().getOutcomes();
		}
		
	};
	
	private final Map<Classification, TIntObjectMap<PosteriorProbability>> posteriorProbabilities;
	private final TIntObjectMap<PriorProbability> priorProbabilities;
	private final Iterable<NaiveBayesProperty> orderedProbabilities;
	private final HandlerRepository<?> featureHandlers;
	
	public CountsProviderNaiveBayesProbabilities(NaiveBayesCountsProvider provider, HandlerRepository<?> featureHandlers){
		
		this.featureHandlers = featureHandlers;
		Counts counts = provider.getCounts();
		
		Map<Classification, TIntIntMap> posteriorTotals = getPosteriorTotals(counts.getPosteriors());
		
		int priorTotal = getPriorTotal(counts.getPriors());
		
		posteriorProbabilities = new THashMap<Classification, TIntObjectMap<PosteriorProbability>>();
		priorProbabilities = new TIntObjectHashMap<PriorProbability>();
		
		for (NaiveBayesCounts<?> posterior: counts.getPosteriors()){
			
			NaiveBayesPosteriorProperty property = (NaiveBayesPosteriorProperty)posterior.getProperty();
			
			TIntObjectMap<PosteriorProbability> features = posteriorProbabilities.get(property.getClassification());
			
			if (features == null){
				features = new TIntObjectHashMap<PosteriorProbability>();
				posteriorProbabilities.put(property.getClassification(), features);
			}
			
			int featureType = property.getFeatureType();
			PosteriorProbability probability = features.get(featureType);
			
			assert (property.getType() == PropertyType.DISCRETE_POSTERIOR_TYPE || property.getType() == PropertyType.DISTRIBUTION_POSTERIOR_TYPE);
			
			if (property.getType() == PropertyType.DISCRETE_POSTERIOR_TYPE){
				probability = convertDiscretePosteriorCounts(PropertyType.DISCRETE_POSTERIOR_TYPE.cast(property), probability, posterior, posteriorTotals.get(property.getClassification()).get(property.getFeatureType()));
			} else { // distribution type
				probability = convertDistributionPosteriorCounts(PropertyType.DISTRIBUTION_POSTERIOR_TYPE.cast(property), (NaiveBayesDistributionCounts)posterior, posteriorTotals.get(property.getClassification()).get(property.getFeatureType()));
			} 
			
			features.put(featureType, probability);
			
		}
		
		for (NaiveBayesCounts<?> prior: counts.getPriors()){
			NaiveBayesPriorProperty property = (NaiveBayesPriorProperty)prior.getProperty();
			
			assert (property.getType() == PropertyType.DISCRETE_PRIOR_TYPE || property.getType() == PropertyType.DISTRIBUTION_PRIOR_TYPE);
			
			PriorProbability probability = priorProbabilities.get(property.getClassificationType());
			
			if (property.getType() == PropertyType.DISCRETE_PRIOR_TYPE){
				probability = convertDiscretePriorCounts(PropertyType.DISCRETE_PRIOR_TYPE.cast(property), probability, prior, priorTotal);
			} else { // distribution type
				probability = convertDistributionPriorCounts(PropertyType.DISCRETE_PRIOR_TYPE.cast(property), (NaiveBayesDistributionCounts)prior, priorTotal);
			}
			
			priorProbabilities.put(property.getClassificationType(), probability);
		}
		
		orderedProbabilities = new Iterable<NaiveBayesProperty>(){ // lazy loaded iterable

			private SortedSet<NaiveBayesProbability> sortedProperties = null;
			
			@Override
			public Iterator<NaiveBayesProperty> iterator() {
				if (sortedProperties == null){
					sortedProperties = new TreeSet<NaiveBayesProbability>(MOST_PROPULAR_OUTCOMES_PROBABILITIES_COMPARATOR);
					
					// add posteriors
					
					for (Entry<Classification, TIntObjectMap<PosteriorProbability>> classification: posteriorProbabilities.entrySet()){
						
						TIntObjectIterator<PosteriorProbability> it = classification.getValue().iterator();
						
						while(it.hasNext()){
						
							it.advance();
							
							PosteriorProbability probabilities = it.value();
							
							assert(probabilities instanceof DiscretePosteriorProbabilities || probabilities instanceof DistributionProbability);
							
							if (probabilities instanceof DiscretePosteriorProbabilities){
								DiscretePosteriorProbabilities discreteProbabilities = (DiscretePosteriorProbabilities)probabilities;
								
								for (Entry<Feature, DiscreteProbability> featureProbability: discreteProbabilities.getFeatureProbabilities()){
									sortedProperties.add(new NaiveBayesProbability(new DiscreteNaiveBayesPosteriorProperty(featureProbability.getKey(), classification.getKey()), featureProbability.getValue()));

								}
							} else {								
								sortedProperties.add(new NaiveBayesProbability(new NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty(it.key(), classification.getKey()), probabilities));
							}
						}
							
					}
					
					// add priors
					
					TIntObjectIterator<PriorProbability> it = priorProbabilities.iterator();
					
					while(it.hasNext()){
						it.advance();
						
						PriorProbability probability = it.value();
						
						assert(probability instanceof DiscretePriorProbabilities || probability instanceof DistributionProbability);
						
						if (probability instanceof DiscretePriorProbabilities){
							for (Entry<Classification, DiscreteProbability> discreteProbilities: ((DiscretePriorProbabilities)probability).getClassificationProbabilities()){
								sortedProperties.add(new NaiveBayesProbability(new DiscreteNaiveBayesPriorProperty(discreteProbilities.getKey()), discreteProbilities.getValue()));
							}
						} else{
							sortedProperties.add(new NaiveBayesProbability(new NaiveBayesProperty.NaiveBayesPriorDistributionProperty(it.key()), probability));
						}
					}
				}
				
				return Iterables.transform(sortedProperties, new Function<NaiveBayesProbability, NaiveBayesProperty>(){

					@Override
					public NaiveBayesProperty apply(NaiveBayesProbability input) {
						return input.getProperty();
					}
					
				}).iterator();
			}
			
		};
	}
	
	private PriorProbability convertDistributionPriorCounts(
			DiscreteNaiveBayesPriorProperty property,
			NaiveBayesDistributionCounts counts,
			int priorTotal) {
		
		ClassificationHandler<?> handler = featureHandlers.getClassificationHandler(property.getClassification().getType());
		
		DistributionProvider distributionProvider = handler.getDistributionProvider();
		
		assert(distributionProvider != null);
		
		return new DistributionProbability(counts.getDistribution(), distributionProvider.getDistribution()); 
	}

	private PriorProbability convertDiscretePriorCounts(
			DiscreteNaiveBayesPriorProperty property, 
			PriorProbability probability,
			NaiveBayesCounts<?> counts, 
			int priorTotal) {
		
		DiscretePriorProbabilities probabilities;
		
		if (probability == null){
			probabilities = new DiscretePriorProbabilities();
		} else{
			probabilities = (DiscretePriorProbabilities)probability;
		}
		
		
		probabilities.addProbability(property.getClassification(), new DiscreteProbability(counts.getCounts(), priorTotal));
		
		return probabilities;
	}

	private PosteriorProbability convertDistributionPosteriorCounts(
			NaiveBayesPosteriorDistributionProperty property,
			NaiveBayesDistributionCounts counts,
			int totalCountsForPosteriorType) {
		
		FeatureHandler<?> handler = featureHandlers.getFeatureHandler(property.getFeatureType());
		
		DistributionProvider distributionProvider = handler.getDistributionProvider();
		
		assert(distributionProvider != null);
		
		return new DistributionProbability(counts.getDistribution(), distributionProvider.getDistribution());
	}

	private PosteriorProbability convertDiscretePosteriorCounts(
			DiscreteNaiveBayesPosteriorProperty property,
			PosteriorProbability probability, 
			NaiveBayesCounts<?> counts,
			int totalCountsForPosteriorType) {
		
		DiscretePosteriorProbabilities probabilities;
		
		if (probability == null){ 
			probabilities = new DiscretePosteriorProbabilities();
		} else{
			probabilities = (DiscretePosteriorProbabilities)probability;
		}
		
		probabilities.addProbability(property.getFeature(), new DiscreteProbability(counts.getCounts(), totalCountsForPosteriorType));
		
		return probabilities;
	}

	@Override
	public String toString(){
		return Iterables.toString(getOrderedProperties());
	}
	
	private int getPriorTotal(Iterable<? extends NaiveBayesCounts<?>> priorsIt) {
		int priorTotal = 0;
		
		for (NaiveBayesCounts<?> prior: priorsIt){
			priorTotal += prior.getCounts();
		}
		
		return priorTotal;
	}

	private Map<Classification, TIntIntMap> getPosteriorTotals(Iterable<? extends NaiveBayesCounts<?>> posteriorsIt) {
		Map<Classification, TIntIntMap> postertiorTotals = new HashMap<Classification, TIntIntMap>();
		
		for (NaiveBayesCounts<?> posteriors: posteriorsIt){
			
			NaiveBayesPosteriorProperty property = (NaiveBayesPosteriorProperty)posteriors.getProperty();
			
			TIntIntMap classificationTotal = postertiorTotals.get(property.getClassification());
			
			if (classificationTotal == null){
				classificationTotal = new TIntIntHashMap();
			}
			
			classificationTotal.adjustOrPutValue(property.getFeatureType(), posteriors.getCounts(), posteriors.getCounts());
			postertiorTotals.put(property.getClassification(), classificationTotal);
		}
		
		return postertiorTotals;
	}

	@Override
	public double getPosteriorProbability(Feature feature, Classification classification) {
		TIntObjectMap<PosteriorProbability> classProbabilities = posteriorProbabilities.get(classification);
		
		double probability = NOMINAL_PROBABILITY;
		
		if (classProbabilities != null){ // TODO if we insist on classification being driven from getAllClassifications, this check is unneeded.
			
			PosteriorProbability probabilities = classProbabilities.get(feature.getType());
			
			if (probabilities != null){
				probability = probabilities.getProbability(feature, classification);
			}
		}
		
		return probability;
	}

	@Override
	public double getPriorProbability(Classification classification) {
		PriorProbability probability = priorProbabilities.get(classification.getType());
		
		if (probability == null){ // TODO if we insist on classification being driven from getAllClassifications, this check is unneeded.
			return NOMINAL_PROBABILITY;
		}
		
		return probability.getProbability(classification);
	}

	@Override
	public Iterable<Classification> getAllClassifications() {
		return Iterables.concat(Iterables.transform(Ints.asList(priorProbabilities.keys()), new Function<Integer, Iterable<Classification>>(){

			@Override
			public Iterable<Classification> apply(Integer classificationType) {
				
				PriorProbability probability = priorProbabilities.get(classificationType);
				
				if (probability instanceof DiscretePriorProbabilities){
					return ((DiscretePriorProbabilities)probability).getAllClassifications();
				} else {
					throw new IllegalStateException("Unable to handle non discrete classification probabilities yet"); // TODO need to work out how this will work!
				}
			}
		}));
	}

	@Override
	public Iterable<NaiveBayesProperty> getOrderedProperties() {
		return orderedProbabilities;
	}
}
