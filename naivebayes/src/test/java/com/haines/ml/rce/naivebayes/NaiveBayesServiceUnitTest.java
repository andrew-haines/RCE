package com.haines.ml.rce.naivebayes;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.accumulator.AccumulatorEventConsumer;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestEvent;
import com.haines.ml.rce.test.TestFeature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class NaiveBayesServiceUnitTest {
	
	private static final TestEvent TEST_EVENT_1 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_2 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature3")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_3 = new TestEvent(Arrays.asList(new TestFeature("feature5"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class2")));
	private static final TestEvent TEST_EVENT_4 = new TestEvent(Arrays.asList(new TestFeature("feature9"), new TestFeature("feature10")), Arrays.asList(new TestClassification("class3")));
	private static final TestEvent TEST_EVENT_5 = new TestEvent(Arrays.asList(new TestFeature("feature11"), new TestFeature("feature12"), new TestFeature("feature9")), Arrays.asList(new TestClassification("class2")));
	private static final TestEvent TEST_EVENT_6 = new TestEvent(Arrays.asList(new TestFeature("feature5"), new TestFeature("feature12"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class2")));
	private static final TestEvent TEST_EVENT_7 = new TestEvent(Arrays.asList(new TestFeature("feature3"), new TestFeature("feature12"), new TestFeature("feature7")), Arrays.asList(new TestClassification("class3")));
	private static final TestEvent TEST_EVENT_8 = new TestEvent(Arrays.asList(new TestFeature("feature5"), new TestFeature("feature1"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_9 = new TestEvent(Arrays.asList(new TestFeature("feature8"), new TestFeature("feature6"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_10 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature6"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class4")));
	private static final TestEvent TEST_EVENT_11 = new TestEvent(Arrays.asList(new TestFeature("feature20"), new TestFeature("feature21"), new TestFeature("feature22")), Arrays.asList(new TestClassification("class4")));
	private static final TestEvent TEST_EVENT_12 = new TestEvent(Arrays.asList(new TestFeature("feature90"), new TestFeature("feature27"), new TestFeature("feature23")), Arrays.asList(new TestClassification("class4")));
	private static final TestEvent TEST_EVENT_13 = new TestEvent(Arrays.asList(new TestFeature("feature90"), new TestFeature("feature65"), new TestFeature("feature29")), Arrays.asList(new TestClassification("class4")));
	private static final Iterable<Feature> TEST_FEATURE_CLASSIFICATION = Arrays.<Feature>asList(new TestFeature("feature90"), new TestFeature("feature6"), new TestFeature("feature2"));
	private static final Iterable<Feature> TEST_FEATURE_CLASSIFICATION2 = Arrays.<Feature>asList(new TestFeature("feature1"), new TestFeature("feature2"), new TestFeature("feature3"));

	private NaiveBayesService candidate;
	
	@Before
	public void before(){
		
		final NaiveBayesIndexes globalIndexes = new NaiveBayesGlobalIndexes();
		
		NaiveBayesIndexes indexes = new NaiveBayesLocalIndexes(new NaiveBayesIndexesProvider() {
			
			@Override
			public NaiveBayesIndexes getIndexes() {
				return globalIndexes;
			}

			@Override
			public void setIndexes(NaiveBayesIndexes indexes) {
				// NoOp
			}
		});
		
		AccumulatorEventConsumer<ClassifiedEvent> eventConsumer = new AccumulatorEventConsumer<ClassifiedEvent>(new RONaiveBayesMapBasedLookupStrategy(indexes));

		consumeTestEvents(eventConsumer);
		
		NaiveBayesAccumulatorBackedCountsProvider provider = new NaiveBayesAccumulatorBackedCountsProvider(eventConsumer.getAccumulatorProvider(), indexes);
		
		final NaiveBayesProbabilities probabilities = new CountsProviderNaiveBayesProbabilities(provider);
		
		this.candidate = new NaiveBayesService(new NaiveBayesProbabilitiesProvider() {
			
			@Override
			public NaiveBayesProbabilities getProbabilities() {
				return probabilities;
			}
		});
	}

	private void consumeTestEvents(AccumulatorEventConsumer<ClassifiedEvent> eventConsumer) {
		eventConsumer.consume(TEST_EVENT_1);
		eventConsumer.consume(TEST_EVENT_2);
		eventConsumer.consume(TEST_EVENT_3);
		eventConsumer.consume(TEST_EVENT_4);
		eventConsumer.consume(TEST_EVENT_5);
		eventConsumer.consume(TEST_EVENT_6);
		eventConsumer.consume(TEST_EVENT_7);
		eventConsumer.consume(TEST_EVENT_8);
		eventConsumer.consume(TEST_EVENT_9);
		eventConsumer.consume(TEST_EVENT_10);
		eventConsumer.consume(TEST_EVENT_11);
		eventConsumer.consume(TEST_EVENT_12);
		eventConsumer.consume(TEST_EVENT_13);
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaximumLikelihoodClassification_thenCorrectClassReturned(){
		Classification classification = candidate.getMaximumLikelihoodClassification(TEST_FEATURE_CLASSIFICATION);
		
		assertThat(classification.toExternalForm(), is(equalTo("class4")));
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaximumLikelihoodClassification2_thenCorrectClassReturned(){
		Classification classification = candidate.getMaximumLikelihoodClassification(TEST_FEATURE_CLASSIFICATION2);
		
		assertThat(classification.toExternalForm(), is(equalTo("class1")));
	}
}
