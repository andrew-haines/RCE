package com.haines.ml.rce.naivebayes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestFeature;

public class CountsProviderNaiveBayesProbabilitiesUnitTest {
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> POSTERIOR_COUNTS = getTestPosteriorCounts(); 
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> POSTERIOR_DIFFERENT_FEATURE_TYPE_COUNTS = getTestDifferentFeatureTypePosteriorCounts(); 
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> POSTERIOR_DUP_COUNTS = getTestPosteriorDupCounts(); 
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> PRIOR_COUNTS = getTestPriorCounts(); 

	private CountsProviderNaiveBayesProbabilities candidate;
	
	@Before
	public void before(){
		this.candidate = new CountsProviderNaiveBayesProbabilities(new NaiveBayesCountsProvider() {
			
			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
				return PRIOR_COUNTS;
			}
			
			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
				return POSTERIOR_COUNTS;
			}
		});
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
			public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
				return PRIOR_COUNTS;
			}
			
			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
				return POSTERIOR_DUP_COUNTS;
			}
		});
	}
	
	@Test
	public void givenCandidate_whenCallingGetPosteriorProbabilitiesWithDifferentFeatureTypes_thenCorrectProbabilitiesReturned(){
		this.candidate = new CountsProviderNaiveBayesProbabilities(new NaiveBayesCountsProvider() {
			
			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
				return PRIOR_COUNTS;
			}
			
			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
				return POSTERIOR_DIFFERENT_FEATURE_TYPE_COUNTS;
			}
		});
		
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

	private static Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getTestPriorCounts() {
		return Arrays.asList(new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class1")), 13),
				new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class2")), 45),
				new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class3")), 2),
				new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 65),
				new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class5")), 8));
	}
	
	private static Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getTestDifferentFeatureTypePosteriorCounts() {
		return Arrays.asList(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1", 1), new TestClassification("class1")), 33),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2", 1), new TestClassification("class1")), 76),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1", 1), new TestClassification("class2")), 22),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2", 1), new TestClassification("class2")), 21),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1", 2), new TestClassification("class1")), 85),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2", 2), new TestClassification("class1")), 45)
				);
	}

	private static Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getTestPosteriorDupCounts() {
		return Arrays.asList(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 33),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class1")), 76),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class2")), 22),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 21),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 85)
				);
	}
	
	private static Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getTestPosteriorCounts() {
		return Arrays.asList(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 33),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class1")), 76),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class2")), 22),
				new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 21)
				);
	}
}
