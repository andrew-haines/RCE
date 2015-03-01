package com.haines.ml.rce.accumulator.handlers;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.haines.ml.rce.accumulator.Accumulator;
import com.haines.ml.rce.accumulator.AccumulatorLookupStrategy;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.model.distribution.DistributionParameters;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestEvent;
import com.haines.ml.rce.test.TestFeature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SequentialDistributionFeatureHandlerUnitTet {
	
	private static final TestFeature TEST_FEATURE1 = new TestFeature(5, 1);
	private static final TestFeature TEST_FEATURE2 = new TestFeature(8, 1);
	private static final TestFeature TEST_FEATURE3 = new TestFeature(3, 1);
	private static final TestFeature TEST_FEATURE4 = new TestFeature(333, 1);
	private static final TestFeature TEST_FEATURE5 = new TestFeature(5, 1);
	
	private static final TestClassification TEST_CLASSIFICATION1 = new TestClassification("testClass1", 1);
	private static final TestClassification TEST_CLASSIFICATION2 = new TestClassification("testClass2", 1);
	
	private static final TestEvent TEST_EVENT_1 = new TestEvent(Lists.newArrayList(TEST_FEATURE1), Lists.newArrayList(TEST_CLASSIFICATION1));
	private static final TestEvent TEST_EVENT_2 = new TestEvent(Lists.newArrayList(TEST_FEATURE2), Lists.newArrayList(TEST_CLASSIFICATION1));
	private static final TestEvent TEST_EVENT_3 = new TestEvent(Lists.newArrayList(TEST_FEATURE3), Lists.newArrayList(TEST_CLASSIFICATION1));
	private static final TestEvent TEST_EVENT_4 = new TestEvent(Lists.newArrayList(TEST_FEATURE4), Lists.newArrayList(TEST_CLASSIFICATION1));
	
	private static final int[] CLASS_2_SLOTS = new int[]{0, 1, 2};

	private SequentialDistributionFeatureHandler<TestEvent> candidate;
	private Accumulator<TestEvent> accumulator;
	private AccumulatorLookupStrategy<TestEvent> lookup;
	
	@SuppressWarnings("unchecked")
	@Before
	public void before(){
		candidate = new SequentialDistributionFeatureHandler<TestEvent>();
		
		lookup = mock(AccumulatorLookupStrategy.class);
		
		when(lookup.getPosteriorSlots(TEST_FEATURE1, TEST_CLASSIFICATION1, 3)).thenReturn(CLASS_2_SLOTS);
		when(lookup.getPosteriorSlots(TEST_FEATURE2, TEST_CLASSIFICATION1, 3)).thenReturn(CLASS_2_SLOTS);
		when(lookup.getPosteriorSlots(TEST_FEATURE3, TEST_CLASSIFICATION1, 3)).thenReturn(CLASS_2_SLOTS);
		when(lookup.getPosteriorSlots(TEST_FEATURE4, TEST_CLASSIFICATION1, 3)).thenReturn(CLASS_2_SLOTS);
		
		accumulator = new ClassifiedEventAccumulatorConsumer<TestEvent>(Accumulator.DEFAULT_CONFIG, lookup, HandlerRepository.<TestEvent>create(ImmutableMap.<Integer, FeatureHandler<TestEvent>>builder().put(1, new SequentialDistributionFeatureHandler()).build(), Collections.<Integer, ClassificationHandler<TestEvent>>emptyMap()));
	}
	
	@Test
	public void givenCandidate_whenCallingIncrementWithSingleEvent_thenCorrectValuesReturned(){
		
		DistributionParameters params = candidate.getDistribution(accumulator, CLASS_2_SLOTS);
		
		assertThat(params.getMean(), is(equalTo(0.0)));
		assertThat(params.getVariance(), is(equalTo(0.0)));
		assertThat(params.getNumSamples(), is(equalTo(0)));
		
		candidate.increment(TEST_FEATURE1, TEST_EVENT_1, accumulator, lookup);
		
		params = candidate.getDistribution(accumulator, CLASS_2_SLOTS);
		
		assertThat(params.getMean(), is(equalTo(5.0)));
		assertThat(params.getVariance(), is(equalTo(Double.NaN)));
		assertThat(params.getNumSamples(), is(equalTo(1)));
	}
	
	@Test
	public void givenCandidate_whenCallingIncrementWithTwoEvents_thenCorrectValuesReturned(){
		
		candidate.increment(TEST_FEATURE1, TEST_EVENT_1, accumulator, lookup);
		candidate.increment(TEST_FEATURE2, TEST_EVENT_2, accumulator, lookup);
		
		DistributionParameters params = candidate.getDistribution(accumulator, CLASS_2_SLOTS);
		
		assertThat(params.getMean(), is(equalTo(6.5)));
		assertThat(params.getVariance(), is(equalTo(4.5)));
		assertThat(params.getNumSamples(), is(equalTo(2)));
	}
	
	@Test
	public void givenCandidate_whenCallingIncrementWithThreeEvents_thenCorrectValuesReturned(){
		
		candidate.increment(TEST_FEATURE1, TEST_EVENT_1, accumulator, lookup);
		candidate.increment(TEST_FEATURE2, TEST_EVENT_2, accumulator, lookup);
		candidate.increment(TEST_FEATURE3, TEST_EVENT_3, accumulator, lookup);
		
		DistributionParameters params = candidate.getDistribution(accumulator, CLASS_2_SLOTS);
		
		assertThat(params.getMean(), is(equalTo(5.333333492279053)));
		assertThat(params.getVariance(), is(equalTo(6.333333492279053)));
		assertThat(params.getNumSamples(), is(equalTo(3)));
	}
	
	@Test
	public void givenCandidate_whenCallingIncrementWithFourEvents_thenCorrectValuesReturned(){
		
		candidate.increment(TEST_FEATURE1, TEST_EVENT_1, accumulator, lookup);
		candidate.increment(TEST_FEATURE2, TEST_EVENT_2, accumulator, lookup);
		candidate.increment(TEST_FEATURE3, TEST_EVENT_3, accumulator, lookup);
		candidate.increment(TEST_FEATURE4, TEST_EVENT_4, accumulator, lookup);
		
		DistributionParameters params = candidate.getDistribution(accumulator, CLASS_2_SLOTS);
		
		assertThat(params.getMean(), is(equalTo(87.25)));
		assertThat(params.getVariance(), is(equalTo(26845.583984375)));
		assertThat(params.getNumSamples(), is(equalTo(4)));
	}
}