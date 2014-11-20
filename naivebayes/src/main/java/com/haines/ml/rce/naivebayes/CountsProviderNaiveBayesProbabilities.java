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

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.Probability;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public class CountsProviderNaiveBayesProbabilities implements NaiveBayesProbabilities{
	
	private static final Comparator<? super NaiveBayesProbability> MOST_PROPULAR_OUTCOMES_PROBABILITIES_COMPARATOR = new Comparator<NaiveBayesProbability>(){

		@Override
		public int compare(NaiveBayesProbability o1, NaiveBayesProbability o2) {
			return o1.getProbability().getOutcomes() - o2.getProbability().getOutcomes();
		}
		
	};
	
	private final Map<Classification, TIntObjectMap<Probabilities>> posteriorProbabilities;
	private final Map<Classification, Probability> priorProbabilities;
	private final Iterable<NaiveBayesProbability> orderedProbabilities;
	
	public CountsProviderNaiveBayesProbabilities(NaiveBayesCountsProvider provider){
		
		Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorsIt = provider.getPosteriorCounts(); //TODO check whether these two not being atomic is ok
		Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> priorsIt = provider.getPriorCounts();
		
		Map<Classification, TIntIntMap> postertiorTotals = getPosteriorTotals(posteriorsIt);
		
		int priorTotal = getPriorTotal(priorsIt); // TODO check that classification priors are calculated by instance seen and not feature seen
		
		posteriorProbabilities = new THashMap<Classification, TIntObjectMap<Probabilities>>();
		priorProbabilities = new THashMap<Classification, Probability>();
		
		for (NaiveBayesCounts<NaiveBayesPosteriorProperty> posteriors: posteriorsIt){
			TIntObjectMap<Probabilities> features = posteriorProbabilities.get(posteriors.getProperty().getClassification());
			
			if (features == null){
				features = new TIntObjectHashMap<Probabilities>();
				posteriorProbabilities.put(posteriors.getProperty().getClassification(), features);
			}
			
			Feature feature = posteriors.getProperty().getFeature();
			Probabilities probabilities = features.get(feature.getType());
			
			if (probabilities == null){
				
				probabilities = new Probabilities(new THashMap<Feature, Probability>());
			}
			
			try{
				Probability probability = new Probability(posteriors.getCounts(), postertiorTotals.get(posteriors.getProperty().getClassification()).get(feature.getType()));
				if (probabilities.probabilities.containsKey(feature)){
					throw new IllegalStateException("posterior feature/class pairing should be unique. Multiple counts supplied for: "+posteriors.getProperty());
				}
				
				probabilities.probabilities.put(feature, probability);
				
				features.put(feature.getType(), probabilities);
			} catch (NullPointerException e){
				System.out.println(postertiorTotals.get(posteriors.getProperty().getClassification()));
				throw e;
			}
			
		}
		
		for (NaiveBayesCounts<NaiveBayesPriorProperty> prior: priorsIt){
			priorProbabilities.put(prior.getProperty().getClassification(), new Probability(prior.getCounts(), priorTotal));
		}
		
		orderedProbabilities = new Iterable<NaiveBayesProbability>(){ // lazy loaded iterable

			private SortedSet<NaiveBayesProbability> sortedProperties = null;
			
			@Override
			public Iterator<NaiveBayesProbability> iterator() {
				if (sortedProperties == null){
					sortedProperties = new TreeSet<NaiveBayesProbability>(MOST_PROPULAR_OUTCOMES_PROBABILITIES_COMPARATOR);
					
					// add posteriors
					
					for (Entry<Classification, TIntObjectMap<Probabilities>> classification: posteriorProbabilities.entrySet()){
						
						TIntObjectIterator<Probabilities> it = classification.getValue().iterator();
						
						while(it.hasNext()){
						
							it.advance();
							
							Probabilities probabilities = it.value();
							
							for (Entry<Feature, Probability> probability: probabilities.probabilities.entrySet()){
							
								sortedProperties.add(new NaiveBayesProbability(new NaiveBayesPosteriorProperty(probability.getKey(), classification.getKey()), probability.getValue()));
							}
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
	
	private int getPriorTotal(Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> priorsIt) {
		int priorTotal = 0;
		
		for (NaiveBayesCounts<NaiveBayesPriorProperty> prior: priorsIt){
			priorTotal += prior.getCounts();
		}
		
		return priorTotal;
	}

	private Map<Classification, TIntIntMap> getPosteriorTotals(Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorsIt) {
		Map<Classification, TIntIntMap> postertiorTotals = new HashMap<Classification, TIntIntMap>();
		
		for (NaiveBayesCounts<NaiveBayesPosteriorProperty> posteriors: posteriorsIt){
			
			TIntIntMap classificationTotal = postertiorTotals.get(posteriors.getProperty().getClassification());
			
			if (classificationTotal == null){
				classificationTotal = new TIntIntHashMap();
			}
			
			classificationTotal.adjustOrPutValue(posteriors.getProperty().getFeature().getType(), posteriors.getCounts(), posteriors.getCounts());
			postertiorTotals.put(posteriors.getProperty().getClassification(), classificationTotal);
		}
		
		return postertiorTotals;
	}

	@Override
	public double getPosteriorProbability(Feature feature, Classification classification) {
		TIntObjectMap<Probabilities> classProbabilities = posteriorProbabilities.get(classification);
		
		Probability probability = null;
		
		if (classProbabilities != null){ // TODO if we insist on classification being driven from getAllClassifications, this check is unneeded.
			
			Probabilities probabilities = classProbabilities.get(feature.getType());
			
			if (probabilities != null){
				probability = probabilities.getProbability(feature);
			}
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
	
	private static class Probabilities {

		private final Map<Feature, Probability> probabilities;
		
		public Probabilities(Map<Feature, Probability> probabilities){
			this.probabilities = probabilities;
		}
		
		public Probability getProbability(Feature value){
			return probabilities.get(value);
		}
	}

}
