package com.haines.ml.rce.accumulator.lookups;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class RONaiveBayesMapBasedLookupStrategyUnitTest {

	private static final TestEvent TEST_EVENT_1 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_2 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature3")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_3 = new TestEvent(Arrays.asList(new TestFeature("feature5"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class2")));

	private RONaiveBayesMapBasedLookupStrategy<TestEvent> candidate;
	
	@Before
	public void before(){
		
		NaiveBayesIndexes globalIndexes = new NaiveBayesGlobalIndexes();
		
		NaiveBayesIndexes localIndexes = new NaiveBayesLocalIndexes(globalIndexes);
		
		candidate = new RONaiveBayesMapBasedLookupStrategy<>(localIndexes);
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetPostiriorIndexes_thenNewIndexesReturned(){
		int[] indexes = candidate.getSlots(TEST_EVENT_1);
		
		assertThat(indexes.length, is(equalTo(3)));
	}
	
	private static class TestEvent implements ClassifiedEvent {

		private final Collection<Feature> features;
		private final Collection<Classification> classifications;
		
		@SuppressWarnings("unchecked")
		private TestEvent(Collection<? extends Feature> features, Collection<? extends Classification> classifications){
			this.features = (Collection<Feature>)features;
			this.classifications = (Collection<Classification>)classifications;
		}
		
		@Override
		public Collection<Feature> getFeatures() {
			return features;
		}

		@Override
		public Collection<Classification> getClassifications() {
			return classifications;
		}
	}
	
	private static class TestFeature implements Feature {
		
		private final String featureStr;
		
		private TestFeature(String featureStr){
			this.featureStr = featureStr;
		}
		
		@Override
		public int hashCode() {
			return featureStr.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return featureStr.equals(((TestFeature)obj).featureStr);
		}
	}
	
	private static class TestClassification implements Classification {
		
		private final String classificationStr;
		
		private TestClassification(String classificationStr){
			this.classificationStr = classificationStr;
		}
		
		@Override
		public int hashCode() {
			return classificationStr.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return classificationStr.equals(((TestClassification)obj).classificationStr);
		}
	}
}
