package com.haines.ml.rce.window;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.haines.ml.rce.model.system.Clock.StaticClock;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestFeature;

public class WindowManagerUnitTest {
	
	private static final int TEST_WINDOW_PERIOD = 15000;
	private static final int TEST_NUM_WINDOWS = 5;
	private static final long TEST_START_TIME = 1398988800000L;
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> TEST_POSTERIOR_EVENTS_1 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 5),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class1")), 15),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 4),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class2")), 4));
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> TEST_POSTERIOR_EVENTS_2 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class4")), 32),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature3"), new TestClassification("class1")), 5),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 88),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class8")), 4));
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> TEST_POSTERIOR_EVENTS_3 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature1"), new TestClassification("class1")), 4),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class1")), 55),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature5"), new TestClassification("class3")), 2),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class4")), 8));
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> TEST_POSTERIOR_EVENTS_4 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature5"), new TestClassification("class1")), 54),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature7"), new TestClassification("class3")), 22),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature8"), new TestClassification("class4")), 89));
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> TEST_POSTERIOR_EVENTS_5 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature3"), new TestClassification("class1")), 6),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature4"), new TestClassification("class1")), 5),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class4")), 9),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class2")), 43),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature8"), new TestClassification("class6")), 86));
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> TEST_POSTERIOR_EVENTS_6 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature5"), new TestClassification("class3")), 33),
			new NaiveBayesCounts<NaiveBayesPosteriorProperty>(new NaiveBayesPosteriorProperty(new TestFeature("feature2"), new TestClassification("class4")), 7));
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_1 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class1")), 45),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class2")), 76)
	);
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_2 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class1")), 22),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class2")), 73),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 11),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class8")), 4)
	);
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_3 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class1")), 33),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class3")), 67),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 54)
	);
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_4 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class1")), 2),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class3")), 95),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 53)
	);
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_5 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class1")), 13),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 96),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class2")), 25),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class6")), 39)
	);
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_6 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class3")), 62),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 74)
	);

	private WindowManager candidate;
	private StaticClock testClock;
	
	@Before
	public void before(){
		
		testClock = new StaticClock(TEST_START_TIME);
		
		candidate = new WindowManager(new WindowConfig(){

			@Override
			public int getWindowPeriod() {
				return TEST_WINDOW_PERIOD;
			}

			@Override
			public int getNumWindows() {
				return TEST_NUM_WINDOWS;
			}
			
		}, testClock);
	}
	
	@Test
	public void givenCandidate_whenAddingSingleEvent_thenEventsAddedToWindow(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1));
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.25)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.75)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
		
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.371900826446281)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.628099173553719)));
	}
	
	@Test
	@Ignore
	public void givenCandidate_whenAddingMultipleEventsInSameWindowPeriod_thenEventsAddedToSameWindow(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1));
		
		testClock.setCurrentTime(TEST_START_TIME + 1000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2));
		
		testClock.setCurrentTime(TEST_START_TIME + 2000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3));
		
		testClock.setCurrentTime(TEST_START_TIME + 3000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_4, TEST_PRIOR_EVENTS_4));
		
		testClock.setCurrentTime(TEST_START_TIME + 4000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_5, TEST_PRIOR_EVENTS_5));
		
		testClock.setCurrentTime(TEST_START_TIME + 5000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_6, TEST_PRIOR_EVENTS_6)); // should not pop events1 off the buffer
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.2)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.75)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
	}
	
	@Test
	@Ignore
	public void givenCandidate_whenAddingMultipleEventsInDifferntWindowPeriodsWithinBuffer_thenEventsNotTruncated(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1));
		
		testClock.setCurrentTime(TEST_START_TIME + 1000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2));
		
		testClock.setCurrentTime(TEST_START_TIME + TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3));
		
		testClock.setCurrentTime(TEST_START_TIME + 2 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_4, TEST_PRIOR_EVENTS_4));
		
		testClock.setCurrentTime(TEST_START_TIME + 3 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_5, TEST_PRIOR_EVENTS_5));
		
		testClock.setCurrentTime(TEST_START_TIME + 4 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_6, TEST_PRIOR_EVENTS_6)); // should not pop events1 off the buffer
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.2)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.75)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
	}
	
	@Test
	@Ignore
	public void givenCandidate_whenAddingMultipleEventsInDifferntWindowPeriodsOutsideOfBuffer_thenEventsTruncated(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1));
		
		testClock.setCurrentTime(TEST_START_TIME + TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2));
		
		testClock.setCurrentTime(TEST_START_TIME + 2 *TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3));
		
		testClock.setCurrentTime(TEST_START_TIME + 3 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_4, TEST_PRIOR_EVENTS_4));
		
		testClock.setCurrentTime(TEST_START_TIME + 4 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_5, TEST_PRIOR_EVENTS_5));
		
		testClock.setCurrentTime(TEST_START_TIME + 6 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_6, TEST_PRIOR_EVENTS_6)); // should not pop events1 off the buffer
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.2)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.75)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
	}

	private NaiveBayesCountsProvider getTestEvents(final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> posteriorEvents, final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> priorEvents) {
		return new NaiveBayesCountsProvider(){

			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts() {
				return posteriorEvents;
			}

			@Override
			public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts() {
				return priorEvents;
			}
			
		};
	}
}
