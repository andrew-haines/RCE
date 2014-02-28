package com.haines.ml.rce.accumulator.lookups;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import com.haines.ml.rce.naivebayes.NaiveBayesGlobalIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes;
import com.haines.ml.rce.naivebayes.NaiveBayesLocalIndexes;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestEvent;
import com.haines.ml.rce.test.TestFeature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public class RONaiveBayesMapBasedLookupStrategyUnitTest {

	private static final TestEvent TEST_EVENT_1 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_2 = new TestEvent(Arrays.asList(new TestFeature("feature1"), new TestFeature("feature3")), Arrays.asList(new TestClassification("class1")));
	private static final TestEvent TEST_EVENT_3 = new TestEvent(Arrays.asList(new TestFeature("feature5"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class2")));
	private static final TestEvent TEST_EVENT_4 = new TestEvent(Arrays.asList(new TestFeature("feature9"), new TestFeature("feature10")), Arrays.asList(new TestClassification("class3")));
	private static final TestEvent TEST_EVENT_5 = new TestEvent(Arrays.asList(new TestFeature("feature11"), new TestFeature("feature12"), new TestFeature("feature2")), Arrays.asList(new TestClassification("class2")));
	
	private RONaiveBayesMapBasedLookupStrategy<TestEvent> candidate;
	
	@Before
	public void before(){
		
		NaiveBayesIndexes globalIndexes = new NaiveBayesGlobalIndexes();
		
		NaiveBayesIndexes localIndexes = new NaiveBayesLocalIndexes(globalIndexes);
		
		candidate = new RONaiveBayesMapBasedLookupStrategy<>(localIndexes);
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetSlots_thenNewIndexesReturned(){
		
		assertThat(candidate.getMaxIndex(), is(equalTo(-1)));
		int[] indexes = candidate.getSlots(TEST_EVENT_1);
		
		assertThat(indexes.length, is(equalTo(3)));
		assertThat(indexes[0], is(equalTo(0)));
		assertThat(indexes[1], is(equalTo(1)));
		assertThat(indexes[2], is(equalTo(2)));
		
		assertThat(candidate.getMaxIndex(), is(equalTo(2)));
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetSlotsFromSameEventAlreadyAdded_thenSameIndexesReturned(){
		int[] indexes = candidate.getSlots(TEST_EVENT_1);
		
		assertThat(indexes.length, is(equalTo(3)));
		assertThat(indexes[0], is(equalTo(0)));
		assertThat(indexes[1], is(equalTo(1)));
		assertThat(indexes[2], is(equalTo(2)));
		
		indexes = candidate.getSlots(TEST_EVENT_1);
		
		assertThat(indexes.length, is(equalTo(3)));
		assertThat(indexes[0], is(equalTo(0)));
		assertThat(indexes[1], is(equalTo(1)));
		assertThat(indexes[2], is(equalTo(2)));
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetSlotsFromDifferentEventAlreadyAdded_thenDifferentIndexesRetuned(){
		int[] indexes = candidate.getSlots(TEST_EVENT_1);
		
		indexes = candidate.getSlots(TEST_EVENT_4);
		
		assertThat(indexes.length, is(equalTo(3)));
		assertThat(indexes[0], is(equalTo(3))); // all different for this event
		assertThat(indexes[1], is(equalTo(4)));
		assertThat(indexes[2], is(equalTo(5)));
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetSlotsFromPartiallyDifferentEventAlreadyAdded_thenSomeDifferentIndexesRetuned(){
		int[] indexes = candidate.getSlots(TEST_EVENT_1);
		
		indexes = candidate.getSlots(TEST_EVENT_2);
		
		assertThat(indexes.length, is(equalTo(3)));
		assertThat(indexes[0], is(equalTo(0))); // this is the same feature/classification pairing as event 1
		assertThat(indexes[1], is(equalTo(3))); // this is different
		assertThat(indexes[2], is(equalTo(2))); // this is the same classification as event 1
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetSlotsFromPartiallyDifferentEvent2AlreadyAdded_thenSomeDifferentIndexesRetuned(){
		int[] indexes = candidate.getSlots(TEST_EVENT_1);
		
		indexes = candidate.getSlots(TEST_EVENT_5);
		
		assertThat(indexes.length, is(equalTo(4)));
		assertThat(indexes[0], is(equalTo(3))); // all different for this event as classification is different
		assertThat(indexes[1], is(equalTo(4)));
		assertThat(indexes[2], is(equalTo(5))); 
		assertThat(indexes[3], is(equalTo(6)));
	}
	
	@Test
	public void givenCandidateWithNoGlobalIndexes_whenCallingGetSlotsFromPartiallyDifferentEvent3AlreadyAdded_thenSomeDifferentIndexesRetuned(){
		int[] indexes = candidate.getSlots(TEST_EVENT_3);
		assertThat(indexes[0], is(equalTo(0)));
		assertThat(indexes[1], is(equalTo(1)));
		assertThat(indexes[2], is(equalTo(2)));
		
		indexes = candidate.getSlots(TEST_EVENT_5);
		
		assertThat(indexes.length, is(equalTo(4)));
		assertThat(indexes[0], is(equalTo(3))); // all different for this event as classification is different
		assertThat(indexes[1], is(equalTo(4)));
		assertThat(indexes[2], is(equalTo(1))); 
		assertThat(indexes[3], is(equalTo(2)));
	}
}
