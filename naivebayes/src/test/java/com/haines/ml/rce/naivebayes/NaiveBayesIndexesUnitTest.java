package com.haines.ml.rce.naivebayes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestFeature;

public class NaiveBayesIndexesUnitTest {
	
	private static final Integer GLOBAL_INDEX_FEATURE1_CLASS1 = 1;
	private static final Integer GLOBAL_INDEX_FEATURE1_CLASS2 = 2;
	private static final Integer GLOBAL_PRIOR_INDEX_CLASS1 = 3;
	
	private static final Integer LOCAL_INDEX_FEATURE2_CLASS1 = 4;
	private static final Integer LOCAL_INDEX_FEATURE2_CLASS2 = 5;
	private static final int[] GLOBAL_INDEX_FOR_TYPE = new int[]{GLOBAL_INDEX_FEATURE1_CLASS1, GLOBAL_INDEX_FEATURE1_CLASS2};

	private NaiveBayesLocalIndexes candidate;
	
	@Before
	public void before(){
		
		NaiveBayesIndexesProvider indexes = new NaiveBayesIndexesProvider() {
			
			@Override
			public NaiveBayesIndexes getIndexes() {
				return new NaiveBayesGlobalIndexes(getGlobalPosteriorIndexes(), getGlobalPriorIndexes(), getGlobalPosteriorTypeIndexes() ,getGlobalPriorTypeIndexes());
			}

			@Override
			public void setIndexes(NaiveBayesIndexes indexes) {
				// NoOp
			}
		};
		
		candidate = new NaiveBayesLocalIndexes(getLocalPosteriorIndexes(), getLocalPriorIndexes(), getLocalPosteriorTypeIndexes(), getLocalPriorTypeIndexes(), indexes);
		candidate.clear();
	}

	@Test
	public void givenCandidate_whenCallingGetIndexFromGlobalVariable_thenCorrectIndexReturned(){
		assertThat(candidate.getDiscretePosteriorIndex(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(GLOBAL_INDEX_FEATURE1_CLASS1)));
		assertThat(candidate.getDiscretePosteriorIndex(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(GLOBAL_INDEX_FEATURE1_CLASS2)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetIndexFromLocalVariable_thenCorrectIndexReturned(){
		assertThat(candidate.getDiscretePosteriorIndex(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(LOCAL_INDEX_FEATURE2_CLASS1)));
		assertThat(candidate.getDiscretePosteriorIndex(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(LOCAL_INDEX_FEATURE2_CLASS2)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetPriorIndexFromGlobalVariable_thenCorrectIndexReturned(){
		assertThat(candidate.getDiscretePriorIndex(new TestClassification("class1")), is(equalTo(GLOBAL_PRIOR_INDEX_CLASS1)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetPriorIndexFromLocalVariable_thenCorrectIndexReturned(){
		assertThat(candidate.getDiscretePriorIndex(new TestClassification("class2")), is(equalTo(4)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaxIndexAfterGlobalPriorIndexesQueried_thenMaxIndexNotIncrementedReturned(){
		candidate.getDiscretePriorIndex(new TestClassification("class1"));
		assertThat(candidate.getMaxIndex(), is(equalTo(3)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaxIndexAfterLocalPriorIndexesCreated_thenMaxIndexIncrementedReturned(){
		candidate.getDiscretePriorIndex(new TestClassification("class2"));
		
		assertThat(candidate.getMaxIndex(), is(equalTo(4)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaxIndex_thenCorrectIndexReturned(){
		assertThat(candidate.getMaxIndex(), is(equalTo(3)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetPosteriorTypeIndexFromGlobalVariable_thenCorrectIndexReturned(){
		assertThat(candidate.getPosteriorDistributionIndexes(new NaiveBayesPosteriorDistributionProperty(0, new TestClassification("class1", 0)), 2), is(equalTo(GLOBAL_INDEX_FOR_TYPE)));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void givenCandidate_whenCallingGetPosteriorTypeIndexFromGlobalVariableWithIncorrectNumslots_thenExceptionThrown(){
		candidate.getPosteriorDistributionIndexes(new NaiveBayesPosteriorDistributionProperty(0, new TestClassification("class1", 0)), 3);
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaxIndexAfterGlobalIndexesQueried_thenMaxIndexNotIncrementedReturned(){
		candidate.getDiscretePosteriorIndex(new TestFeature("feature1"), new TestClassification("class1"));
		candidate.getDiscretePosteriorIndex(new TestFeature("feature1"), new TestClassification("class2"));
		assertThat(candidate.getMaxIndex(), is(equalTo(3)));
	}
	
	@Test
	public void givenCandidate_whenCallingGetMaxIndexAfterLocalIndexesCreated_thenMaxIndexIncrementedReturned(){
		candidate.getDiscretePosteriorIndex(new TestFeature("feature2"), new TestClassification("class1"));
		candidate.getDiscretePosteriorIndex(new TestFeature("feature2"), new TestClassification("class2"));
		assertThat(candidate.getMaxIndex(), is(equalTo(5)));
	}

	private Map<Classification, Integer> getLocalPriorIndexes() {
		return new HashMap<Classification, Integer>();
	}

	private Map<Classification, Map<Feature, Integer>> getLocalPosteriorIndexes() {
		return new HashMap<Classification, Map<Feature, Integer>>();
	}
	
	private Map<NaiveBayesPosteriorDistributionProperty, int[]> getLocalPosteriorTypeIndexes() {
		return new HashMap<NaiveBayesPosteriorDistributionProperty, int[]>();
	}
	
	private Map<Integer, int[]> getLocalPriorTypeIndexes(){
		return new HashMap<Integer, int[]>();
	}
	private Map<Classification, Integer> getGlobalPriorIndexes() {
		Map<Classification, Integer> priorIndexes = new HashMap<Classification, Integer>();
		
		priorIndexes.put(new TestClassification("class1"), 3);
		
		return priorIndexes;
	}

	private Map<Classification, Map<Feature, Integer>> getGlobalPosteriorIndexes() {
		Map<Classification, Map<Feature, Integer>> posteriorIndexes = new HashMap<Classification, Map<Feature, Integer>>();
		
		Map<Feature, Integer> class1Indexes = new HashMap<Feature, Integer>();
		
		class1Indexes.put(new TestFeature("feature1"), GLOBAL_INDEX_FEATURE1_CLASS1);
		
		Map<Feature, Integer> class2Indexes = new HashMap<Feature, Integer>();
		
		class2Indexes.put(new TestFeature("feature1"), GLOBAL_INDEX_FEATURE1_CLASS2);
		
		posteriorIndexes.put(new TestClassification("class1"), class1Indexes);
		posteriorIndexes.put(new TestClassification("class2"), class2Indexes);
		
		return posteriorIndexes;
	}
	
	private Map<NaiveBayesPosteriorDistributionProperty, int[]> getGlobalPosteriorTypeIndexes() {
		Map<NaiveBayesPosteriorDistributionProperty, int[]> posteriorTypesIndexes = new HashMap<NaiveBayesPosteriorDistributionProperty, int[]>();
		
		posteriorTypesIndexes.put(new NaiveBayesPosteriorDistributionProperty(0, new TestClassification("class1", 0)), GLOBAL_INDEX_FOR_TYPE);
		
		return posteriorTypesIndexes;
	}
	
	private Map<Integer, int[]> getGlobalPriorTypeIndexes(){
		Map<Integer, int[]> posteriorTypesIndexes = new HashMap<Integer, int[]>();
		
		posteriorTypesIndexes.put(0, GLOBAL_INDEX_FOR_TYPE);
		
		return posteriorTypesIndexes;
	}
}
