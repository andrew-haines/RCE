package com.haines.ml.rce.naivebayes;

import gnu.trove.map.hash.THashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorDistributionProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;

public abstract class DefaultNaiveBayesIndexes implements NaiveBayesIndexes {
	
	private final static Logger LOG = LoggerFactory.getLogger(DefaultNaiveBayesIndexes.class);

	private static final Function<? super Classification, ? extends DiscreteNaiveBayesPriorProperty> CLASSIFICATION_TO_PRIOR_PROPERTY_FUNC = new Function<Classification, DiscreteNaiveBayesPriorProperty>(){

		@Override
		public DiscreteNaiveBayesPriorProperty apply(Classification input) {
			return new DiscreteNaiveBayesPriorProperty(input);
		}
	};

	private static final Function<? super Integer, ? extends NaiveBayesPriorDistributionProperty> PRIOR_TYPE_TRANSFORM_FUNCTION = new Function<Integer, NaiveBayesPriorDistributionProperty>(){

			@Override
			public NaiveBayesPriorDistributionProperty apply(Integer input) {
				return new NaiveBayesPriorDistributionProperty(input);
			}
		};
	
	protected final Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes;
	protected final Map<Classification, Integer> priorProbabilityIndexes;
	protected final Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes;
	protected final Map<Integer, int[]> priorTypeIndexes;
	protected int maxIndex;
	
	protected DefaultNaiveBayesIndexes(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes, Map<Classification, Integer> priorProbabilityIndexes, Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypeIndexes, Map<Integer, int[]> priorTypeIndexes, int maxIndex){
		
		this.posteriorProbabilityIndexes = checkIsEmpty(posteriorProbabilityIndexes);
		this.priorProbabilityIndexes = checkIsEmpty(priorProbabilityIndexes);
		this.posteriorTypeIndexes = checkIsEmpty(posteriorTypeIndexes);
		this.priorTypeIndexes = checkIsEmpty(priorTypeIndexes);
		
		this.maxIndex = maxIndex;
	}
	
	public int getDiscretePosteriorIndex(Feature feature, Classification classification){
		
		Map<Feature, Integer> innerMap = posteriorProbabilityIndexes.get(classification);
		
		if (innerMap != null){
			Integer index = innerMap.get(feature);
			
			if (index != null){
				return index;
			}
		}
		return NO_INDEX_FOUND;
	}

	public int getDiscretePriorIndex(Classification classification){
		
		Integer index = priorProbabilityIndexes.get(classification);
		
		if (index != null){
			return index;
		}
		return NO_INDEX_FOUND;
	}
	
	public final int getMaxIndex(){
		return maxIndex;
	}
	
	protected <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
		if (!map.isEmpty()){
			throw new IllegalArgumentException("Map passed into global index must be empty");
		}
		return map;
	}
	
	@Override
	public int[] getPosteriorDistributionIndexes(NaiveBayesPosteriorDistributionProperty types, int numIdxes) {
		
		int[] indexes = posteriorTypeIndexes.get(types);
		
		if (indexes != null){
			checkIndexLength(types, indexes, numIdxes);
			
			return indexes;
		}
		
		return NO_INDEXES_FOUND;
	}

	@Override
	public int[] getPriorDistributionIndexes(int classificationIndex, int numIndexes) {
		int[] indexes = priorTypeIndexes.get(classificationIndex);
		
		if (indexes != null){
			checkIndexLength(null, indexes, numIndexes);
			
			return indexes;
		}
		
		return NO_INDEXES_FOUND;
	}

	protected final void checkIndexLength(NaiveBayesPosteriorDistributionProperty types, int[] indexes, int numIdxes) {
		if (indexes.length != numIdxes && numIdxes != UNKNOWN_NUM_INDEXES){
			throw new IllegalArgumentException("Existing slots for type: "+types+" does not have the required num of requested slots. Wanted: "+numIdxes+" slots, found: "+numIdxes+" slots");
		}
	}
	
	@Override
	public Iterable<NaiveBayesPosteriorDistributionProperty> getPosteriorDistributionsTypes(){
		return posteriorTypeIndexes.keySet();
	}
	
	@Override
	public Iterable<NaiveBayesPriorDistributionProperty> getPriorDistributionTypes(){
		return Iterables.transform(priorTypeIndexes.keySet(), PRIOR_TYPE_TRANSFORM_FUNCTION);
	}

	public Iterable<DiscreteNaiveBayesPosteriorProperty> getDiscretePosteriors(){
		return new Iterable<DiscreteNaiveBayesPosteriorProperty>(){

			@Override
			public Iterator<DiscreteNaiveBayesPosteriorProperty> iterator() {
				
				final Iterator<Map.Entry<Classification, Map<Feature, Integer>>> keySetIt = new THashMap<Classification, Map<Feature, Integer>>(posteriorProbabilityIndexes).entrySet().iterator();
				return new Iterator<DiscreteNaiveBayesPosteriorProperty>(){

					private Iterator<Feature> currentPosteriorFeatureForClassification;
					private Classification currentClassification;
					
					@Override
					public boolean hasNext() {
						return keySetIt.hasNext() || (currentPosteriorFeatureForClassification != null && currentPosteriorFeatureForClassification.hasNext());
					}

					@Override
					public DiscreteNaiveBayesPosteriorProperty next() {
						if (currentPosteriorFeatureForClassification == null || !currentPosteriorFeatureForClassification.hasNext()){
							
							Map.Entry<Classification, Map<Feature, Integer>> entry = keySetIt.next();
							
							currentClassification = entry.getKey();
							currentPosteriorFeatureForClassification = entry.getValue().keySet().iterator();
						}
						
						return new DiscreteNaiveBayesPosteriorProperty(currentPosteriorFeatureForClassification.next(), currentClassification);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException("Backing array is immutable");
					}
				};
			}
		};
	}

	public Iterable<DiscreteNaiveBayesPriorProperty> getDiscretePriors() {
		return Iterables.transform(new THashMap<>(priorProbabilityIndexes).keySet(), CLASSIFICATION_TO_PRIOR_PROPERTY_FUNC);
	}

	public void clear() {
		posteriorProbabilityIndexes.clear();
		priorProbabilityIndexes.clear();
		posteriorTypeIndexes.clear();
		priorTypeIndexes.clear();
		
		maxIndex = 0;
	}
	
	public abstract NaiveBayesIndexesProvider getGlobalIndexes();

	public NaiveBayesIndexes copy() {
		
		final NaiveBayesIndexesProvider globalIndexes = this.getGlobalIndexes();
		
		return new DefaultNaiveBayesIndexes(copyPosterior(posteriorProbabilityIndexes), copyPriors(priorProbabilityIndexes), copyPosteriorTypes(posteriorTypeIndexes), copyPriorTypes(priorTypeIndexes), this.getMaxIndex()) {
			
			@Override
			public NaiveBayesIndexesProvider getGlobalIndexes() {
				return globalIndexes;
			}

			@Override
			protected <K, V> Map<K, V> checkIsEmpty(Map<K, V> map) {
				return map;
			}
			
			public String toString(){
				return "Copy Indexes";
			}
		};
	}

	private final static Map<Classification, Integer> copyPriors(Map<Classification, Integer> priorProbabilityIndexes) {
		return ImmutableMap.copyOf(priorProbabilityIndexes);
	}
	
	private final static Map<NaiveBayesPosteriorDistributionProperty, int[]> copyPosteriorTypes(Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypes){
		return ImmutableMap.copyOf(posteriorTypes);
	}
	
	private final static Map<Integer, int[]> copyPriorTypes(Map<Integer, int[]> priorTypes){
		return ImmutableMap.copyOf(priorTypes);
	}

	private final static  Map<Classification, Map<Feature, Integer>> copyPosterior(Map<Classification, Map<Feature, Integer>> posteriorProbabilityIndexes) {
		Map<Classification, Map<Feature, Integer>> mapCopy = new THashMap<Classification, Map<Feature,Integer>>();
		
		for(Entry<Classification, Map<Feature, Integer>> entry: posteriorProbabilityIndexes.entrySet()){
			mapCopy.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
		}
		
		return mapCopy;
	}
}
