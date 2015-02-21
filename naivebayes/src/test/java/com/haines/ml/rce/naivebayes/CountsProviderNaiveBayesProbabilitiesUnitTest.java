package com.haines.ml.rce.naivebayes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.DiscreteNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.DiscreteNaiveBayesPriorProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestFeature;

public class CountsProviderNaiveBayesProbabilitiesUnitTest {
	
	private static final Iterable<? extends NaiveBayesCounts<?>> POSTERIOR_COUNTS = getTestPosteriorCounts(); 
	private static final Iterable<? extends NaiveBayesCounts<?>> POSTERIOR_DIFFERENT_FEATURE_TYPE_COUNTS = getTestDifferentFeatureTypePosteriorCounts(); 
	private static final Iterable<? extends NaiveBayesCounts<?>> POSTERIOR_DUP_COUNTS = getTestPosteriorDupCounts(); 
	private static final Iterable<? extends NaiveBayesCounts<?>> PRIOR_COUNTS = getTestPriorCounts(); 

	private CountsProviderNaiveBayesProbabilities candidate;
	
	@Before
	public void before(){
		this.candidate = new CountsProviderNaiveBayesProbabilities(new NaiveBayesCountsProvider() {

			@Override
			public Counts getCounts() {
				return new Counts(){

					@SuppressWarnings("unchecked")
					@Override
					public Iterable<NaiveBayesCounts<?>> getPriors() {
						return (Iterable<NaiveBayesCounts<?>>)PRIOR_COUNTS;
					}

					@SuppressWarnings("unchecked")
					@Override
					public Iterable<NaiveBayesCounts<?>> getPosteriors() {
						return (Iterable<NaiveBayesCounts<?>>)POSTERIOR_COUNTS;
					}
					
				};
			}
		}, null);
	}

	@Test
	public void givenCandidate_whenCallingGetPosteriorProbabilities_thenCorrectProbabilitiesReturned(){
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.30275229357798167)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.6972477064220184)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(0.5116279069767442)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.4883720930232558)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetPosteriorProbabilitiesForUnknownFeature_thenNominalProbabilityReturned(){
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(CountsProviderNaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class3")), is(equalTo(CountsProviderNaiveBayesProbabilities.NOMINAL_PROBABILITY)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetPriorProbabilitiesForUnknownFeature_thenNominalProbabilityReturned(){
		assertThat(candidate.getPriorProbability(new TestClassification("class6")), is(equalTo(CountsProviderNaiveBayesProbabilities.NOMINAL_PROBABILITY)));
	}
	
	@Test(expected=IllegalStateException.class)
	public void givenCandidate_whenCallingGetPosteriorProbabilitiesWithDuplicates_thenIllegalStateExceptionThrown(){
		this.candidate = new CountsProviderNaiveBayesProbabilities(new NaiveBayesCountsProvider() {

			@Override
			public Counts getCounts() {
				return new Counts(){

					@Override
					public Iterable<NaiveBayesCounts<?>> getPriors() {
						return (Iterable<NaiveBayesCounts<?>>)PRIOR_COUNTS;
					}

					@Override
					public Iterable<NaiveBayesCounts<?>> getPosteriors() {
						return (Iterable<NaiveBayesCounts<?>>)POSTERIOR_DUP_COUNTS;
					}
					
				};
			}
		}, null);
	}
	
	@Test
	public void givenCandidate_whenCallingGetPosteriorProbabilitiesWithDifferentFeatureTypes_thenCorrectProbabilitiesReturned(){
		this.candidate = new CountsProviderNaiveBayesProbabilities(new NaiveBayesCountsProvider() {
			
			@Override
			public Counts getCounts() {
				return new Counts(){

					@SuppressWarnings("unchecked")
					@Override
					public Iterable<NaiveBayesCounts<?>> getPriors() {
						return (Iterable<NaiveBayesCounts<?>>)PRIOR_COUNTS;
					}

					@SuppressWarnings("unchecked")
					@Override
					public Iterable<NaiveBayesCounts<?>> getPosteriors() {
						return (Iterable<NaiveBayesCounts<?>>)POSTERIOR_DIFFERENT_FEATURE_TYPE_COUNTS;
					}
					
				};
			}
		}, null);
		
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature1", 1), new TestClassification("class1")), is(equalTo(0.30275229357798167)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature2", 1), new TestClassification("class1")), is(equalTo(0.6972477064220184)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature1", 1), new TestClassification("class2")), is(equalTo(0.5116279069767442)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature2", 1), new TestClassification("class2")), is(equalTo(0.4883720930232558)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature1", 2), new TestClassification("class1")), is(equalTo(0.6538461538461539)));
		assertThat(candidate.getPosteriorProbability(new TestFeature("feature2", 2), new TestClassification("class1")), is(equalTo(0.34615384615384615)));

	}
	
	@Test
	public void givenCandidate_whenCallingGetPriorProbabilities_thenCorrectProbabilitiesReturned(){
		assertThat(candidate.getPriorProbability(new TestClassification("class1")), is(equalTo(0.09774436090225563)));
		assertThat(candidate.getPriorProbability(new TestClassification("class2")), is(equalTo(0.3383458646616541)));
		assertThat(candidate.getPriorProbability(new TestClassification("class3")), is(equalTo(0.015037593984962405)));
		assertThat(candidate.getPriorProbability(new TestClassification("class4")), is(equalTo(0.48872180451127817)));
		assertThat(candidate.getPriorProbability(new TestClassification("class5")), is(equalTo(0.06015037593984962)));
	}

	private static Iterable<? extends NaiveBayesCounts<?>> getTestPriorCounts() {
		return Arrays.asList(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class1")), 13),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class2")), 45),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class3")), 2),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class4")), 65),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPriorProperty(new TestClassification("class5")), 8));
	}
	
	private static Iterable<? extends NaiveBayesCounts<?>> getTestDifferentFeatureTypePosteriorCounts() {
		return Arrays.asList(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1", 1), new TestClassification("class1")), 33),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2", 1), new TestClassification("class1")), 76),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1", 1), new TestClassification("class2")), 22),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2", 1), new TestClassification("class2")), 21),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1", 2), new TestClassification("class1")), 85),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2", 2), new TestClassification("class1")), 45)
				);
	}

	private static Iterable<? extends NaiveBayesCounts<?>> getTestPosteriorDupCounts() {
		return Arrays.asList(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 33),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class1")), 76),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class2")), 22),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 21),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 85)
				);
	}
	
	private static Iterable<? extends NaiveBayesCounts<?>> getTestPosteriorCounts() {
		return Arrays.asList(new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 33),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class1")), 76),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class2")), 22),
				new DiscreteNaiveBayesCounts(new DiscreteNaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 21)
				);
	}
}
