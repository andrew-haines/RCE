package com.haines.ml.rce.aggregator;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestFeature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

public class AggregatorUnitTest {
	
	private static final Feature FEATURE1 = new TestFeature("feature1");
	private static final Feature FEATURE2 = new TestFeature("feature2");
	private static final Feature FEATURE3 = new TestFeature("feature3");
	private static final Feature FEATURE4 = new TestFeature("feature4");
	private static final Classification CLASSIFICATION1 = new TestClassification("class1");
	private static final Classification CLASSIFICATION2 = new TestClassification("class2");

	private Aggregator candidate;
	
	@Before
	public void before(){
		candidate = Aggregator.newInstance();
	}
	
	@Test
	public void givenCandidate_whenCallingAggregate_thenAggregatedAppropriately(){
		
		Collection<NaiveBayesCounts<? extends NaiveBayesProperty>> counts = new ArrayList<NaiveBayesCounts<?>>();
		
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION1), 14));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE2, CLASSIFICATION1), 43));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION2), 56));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION1), 767));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE2, CLASSIFICATION1), 53));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE2, CLASSIFICATION2), 33));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE4, CLASSIFICATION1), 235));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE3, CLASSIFICATION2), 4));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE4, CLASSIFICATION1), 164));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE4, CLASSIFICATION2), 47));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE2, CLASSIFICATION1), 32));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION2), 27));
		counts.add(new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION2), 56));

		counts.add(new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(CLASSIFICATION1), 56));
		counts.add(new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(CLASSIFICATION2), 6));
		counts.add(new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(CLASSIFICATION2), 23));
		counts.add(new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(CLASSIFICATION2), 77));
		counts.add(new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(CLASSIFICATION1), 95));
		
		candidate.aggregate(counts);
		
		Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorCounts = candidate.getAccumulatedPosteriorCounts();
		Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> priorCounts = candidate.getAccumulatedPriorCounts();

		assertThat(Iterables.size(posteriorCounts), is(equalTo(7)));
		assertThat(Iterables.size(priorCounts), is(equalTo(2)));
		
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION1), 781)));
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE2, CLASSIFICATION1), 128)));
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE1, CLASSIFICATION2), 139)));
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE2, CLASSIFICATION2), 33)));
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE4, CLASSIFICATION1), 399)));
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE3, CLASSIFICATION2), 4)));
		assertThat(posteriorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPosteriorProperty(FEATURE4, CLASSIFICATION2), 47)));
		
		assertThat(priorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPriorProperty(CLASSIFICATION1), 151)));
		assertThat(priorCounts, hasItem(new NaiveBayesCounts<>(new NaiveBayesPriorProperty(CLASSIFICATION2), 106)));
	}
}
