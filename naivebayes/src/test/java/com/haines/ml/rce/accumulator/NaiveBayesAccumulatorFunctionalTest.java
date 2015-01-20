package com.haines.ml.rce.accumulator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.handlers.ClassifiedEventAccumulatorConsumer;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesAccumulatorBackedCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider.Counts;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes.VolatileNaiveBayesGlobalIndexesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesLocalIndexes;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.DiscreteNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestEvent;
import com.haines.ml.rce.test.TestFeature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.equalTo;

public class NaiveBayesAccumulatorFunctionalTest {

	private Accumulator<TestEvent> candidate;
	private NaiveBayesLocalIndexes localIndexes;
	private NaiveBayesIndexesProvider globalIndexes;
	private FeatureHandlerRepository<TestEvent> featureHandlers;
	
	@Before
	public void before(){
		
		globalIndexes = new VolatileNaiveBayesGlobalIndexesProvider(new NaiveBayesGlobalIndexes());
		
		localIndexes = new NaiveBayesLocalIndexes(globalIndexes);
		
		RONaiveBayesMapBasedLookupStrategy<TestEvent> lookup = new RONaiveBayesMapBasedLookupStrategy<TestEvent>(localIndexes);
		
		featureHandlers = FeatureHandlerRepository.create();
		
		candidate = new ClassifiedEventAccumulatorConsumer<TestEvent>(Accumulator.DEFAULT_CONFIG, lookup, featureHandlers);
	}
	
	@Test
	public void givenEmptyIndexes_whenCallingGetAccumlatorProvider_thenCountsCalculatedAsEmpty(){
		AccumulatorProvider<TestEvent> accProvider = candidate.getAccumulatorProvider();
		
		NaiveBayesAccumulatorBackedCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accProvider, localIndexes, featureHandlers);
		
		Counts counts = countsProvider.getCounts();
		
		assertThat(counts.getPosteriors(), is(emptyIterable()));
		assertThat(counts.getPriors(), is(emptyIterable()));
	}
	
	@Test
	public void givenEmptyLocalIndexes_whenCallingGetAccumlatorProviderAfter1EventAdded_thenCountsWithAppropriateCountsSet(){
		
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		
		AccumulatorProvider<TestEvent> accProvider = candidate.getAccumulatorProvider();
		
		NaiveBayesAccumulatorBackedCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accProvider, localIndexes, featureHandlers);
		
		Counts counts = countsProvider.getCounts();
		
		assertThat(counts.getPosteriors(), is(not(emptyIterable())));
		assertThat(counts.getPriors(), is(not(emptyIterable())));
		
		assertThat(Iterables.size(counts.getPosteriors()), is(equalTo(2)));
		assertThat(Iterables.size(counts.getPriors()), is(equalTo(1)));
		
		assertThat(counts.getPosteriors(), Matchers.<NaiveBayesCounts<?>>contains(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 1), new TestClassification("class1", 1)), 1),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("false", 2), new TestClassification("class1", 1)), 1)));
		
		assertThat(counts.getPriors(), Matchers.<NaiveBayesCounts<?>>contains(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class1", 1)), 1)));
	}
	
	@Test
	public void givenEmptyLocalIndexes_whenCallingGetAccumlatorProviderAfterMultipleEventsAdded_thenCountsWithAppropriateCountsSet(){
		
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("false", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("true", 2)), Arrays.asList(new TestClassification("class2", 1))));
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		
		AccumulatorProvider<TestEvent> accProvider = candidate.getAccumulatorProvider();
		
		NaiveBayesAccumulatorBackedCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accProvider, localIndexes, featureHandlers);
		
		Counts counts = countsProvider.getCounts();
		
		assertThat(counts.getPosteriors(), is(not(emptyIterable())));
		assertThat(counts.getPriors(), is(not(emptyIterable())));
		
		assertThat(Iterables.size(counts.getPosteriors()), is(equalTo(5)));
		assertThat(Iterables.size(counts.getPriors()), is(equalTo(2)));
		
		assertThat(counts.getPosteriors(), Matchers.<NaiveBayesCounts<?>>containsInAnyOrder(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 1), new TestClassification("class1", 1)), 2),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("false", 2), new TestClassification("class1", 1)), 3),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("false", 1), new TestClassification("class1", 1)), 1),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 1), new TestClassification("class2", 1)), 1),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 2), new TestClassification("class2", 1)), 1)));
		
		assertThat(counts.getPriors(), Matchers.<NaiveBayesCounts<?>>containsInAnyOrder(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class1", 1)), 3),
																			  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class2", 1)), 1)));
	}
	
	@Test
	public void givenFilledGlobalIndexes_whenCallingGetAccumlatorProviderAfterMultipleEventsAdded_thenCountsWithAppropriateCountsSet(){
		
		globalIndexes.setIndexes(new NaiveBayesGlobalIndexes(getTestPosteriorIndexes(), getTestPriorIndexes(), Collections.<NaiveBayesPosteriorDistributionProperty, int[]>emptyMap(), Collections.<Integer, int[]>emptyMap()));
		
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("false", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("true", 2)), Arrays.asList(new TestClassification("class2", 1))));
		candidate.consume(new TestEvent(Arrays.asList(new TestFeature("true", 1), new TestFeature("false", 2)), Arrays.asList(new TestClassification("class1", 1))));
		
		AccumulatorProvider<TestEvent> accProvider = candidate.getAccumulatorProvider();
		
		NaiveBayesAccumulatorBackedCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accProvider, localIndexes, featureHandlers);
		
		Counts counts = countsProvider.getCounts();
		
		assertThat(counts.getPosteriors(), is(not(emptyIterable())));
		assertThat(counts.getPriors(), is(not(emptyIterable())));
		
		assertThat(Iterables.size(counts.getPosteriors()), is(equalTo(5)));
		assertThat(Iterables.size(counts.getPriors()), is(equalTo(2)));
		
		assertThat(counts.getPosteriors(), Matchers.<NaiveBayesCounts<?>>containsInAnyOrder(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 1), new TestClassification("class1", 1)), 2),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("false", 2), new TestClassification("class1", 1)), 3),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("false", 1), new TestClassification("class1", 1)), 1),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 1), new TestClassification("class2", 1)), 1),
																				  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("true", 2), new TestClassification("class2", 1)), 1)));
		
		assertThat(counts.getPriors(), Matchers.<NaiveBayesCounts<?>>containsInAnyOrder(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class1", 1)), 3),
																			  new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class2", 1)), 1)));
	}

	private Map<Classification, Integer> getTestPriorIndexes() {
		return ImmutableMap.<Classification, Integer>builder().put(new TestClassification("class1", 1), 0).build();
	}

	private Map<Classification, Map<Feature, Integer>> getTestPosteriorIndexes() {
		return ImmutableMap.<Classification, Map<Feature, Integer>>builder().put(new TestClassification("class1", 1), ImmutableMap.<Feature, Integer>builder().
				
																													  put(new TestFeature("true", 1), 1).
																													  put(new TestFeature("false", 2), 2).build()).build();
	}
}
