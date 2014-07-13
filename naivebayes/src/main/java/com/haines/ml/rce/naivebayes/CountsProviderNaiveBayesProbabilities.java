package com.haines.ml.rce.naivebayes;

import gnu.trove.map.hash.THashMap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.Probability;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public class CountsProviderNaiveBayesProbabilities implements NaiveBayesProbabilities{
	
	private final Map<Classification, Map<Feature, Probability>> posteriorProbabilities;
	private final Map<Classification, Probability> priorProbabilities;
	private final Iterable<NaiveBayesProbability> orderedProbabilities;
	
	public CountsProviderNaiveBayesProbabilities(NaiveBayesCountsProvider provider){
		
		Map<Classification, Integer> postertiorTotals = getPosteriorTotals(provider);
		
		int priorTotal = getPriorTotal(provider); // TODO check that classification priors are calculated by instance seen and not feature seen
		
		posteriorProbabilities = new THashMap<Classification, Map<Feature, Probability>>();
		priorProbabilities = new THashMap<Classification, Probability>();
		
		for (NaiveBayesCounts<NaiveBayesPosteriorProperty> posteriors: provider.getPosteriorCounts()){
			Map<Feature, Probability> features = posteriorProbabilities.get(posteriors.getProperty().getClassification());
			
			if (features == null){
				features = new THashMap<Feature, Probability>();
				posteriorProbabilities.put(posteriors.getProperty().getClassification(), features);
			}
			
			if (features.containsKey(posteriors.getProperty().getFeature())){
				throw new IllegalStateException("posterior feature/class pairing should be unique. Multiple counts supplied for: "+posteriors.getProperty());
			}
			
			features.put(posteriors.getProperty().getFeature(), new Probability(posteriors.getCounts(), postertiorTotals.get(posteriors.getProperty().getClassification())));
		}
		
		for (NaiveBayesCounts<NaiveBayesPriorProperty> prior: provider.getPriorCounts()){
			priorProbabilities.put(prior.getProperty().getClassification(), new Probability(prior.getCounts(), priorTotal));
		}
		
		orderedProbabilities = new Iterable<NaiveBayesProbability>(){ // lazy loaded iterable

			private SortedSet<NaiveBayesProbability> sortedProperties = null;
			
			@Override
			public Iterator<NaiveBayesProbability> iterator() {
				if (sortedProperties == null){
					sortedProperties = new TreeSet<NaiveBayesProbability>();
					
					// add posteriors
					
					for (Entry<Classification, Map<Feature,Probability>> classification: posteriorProbabilities.entrySet()){
						for (Entry<Feature, Probability> feature: classification.getValue().entrySet()){
							sortedProperties.add(new NaiveBayesProbability(new NaiveBayesPosteriorProperty(feature.getKey(), classification.getKey()), feature.getValue()));
						}
					}
					
					// add priors
					
					for (Entry<Classification, Probability> classification: priorProbabilities.entrySet()){
						sortedProperties.add(new NaiveBayesProbability(new NaiveBayesPriorProperty(classification.getKey()), classification.getValue()));
					}
				}
				
				return sortedProperties.iterator();
			}
			
		};
	}
	
	private int getPriorTotal(NaiveBayesCountsProvider provider) {
		int priorTotal = 0;
		
		for (NaiveBayesCounts<NaiveBayesPriorProperty> prior: provider.getPriorCounts()){
			priorTotal += prior.getCounts();
		}
		
		return priorTotal;
	}

	private Map<Classification, Integer> getPosteriorTotals(NaiveBayesCountsProvider provider) {
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
		Map<Feature, Probability>  classProbabilities = posteriorProbabilities.get(classification);
		
		Probability probability = null;
		
		if (classProbabilities != null){ // TODO if we insist on classification being driven from getAllClassifications, this check is unneeded.
			
			probability = classProbabilities.get(feature);
		}
		
		if (probability == null){
			// note that we cant use +1 smoothing or other scheme as we dont know what the complete feature space is. 
			// Use a nominal probability instead to avoid multipling by zero. The value of this norminal probability
			// might need to be tuned
			return NOMINAL_PROBABILITY; 
		}
		
		return probability.getProbability();
	}

	@Override
	public double getPriorProbability(Classification classification) {
		Probability probability = priorProbabilities.get(classification);
		
		if (probability == null){ // TODO if we insist on classification being driven from getAllClassifications, this check is unneeded.
			return NOMINAL_PROBABILITY;
		}
		
		return probability.getProbability();
	}

	@Override
	public Iterable<Classification> getAllClassifications() {
		return priorProbabilities.keySet();
	}

	@Override
	public Iterable<NaiveBayesProbability> getOrderedProbabilities() {
		return orderedProbabilities;
	}
}
