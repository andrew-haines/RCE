package com.haines.ml.rce.window;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.model.system.Clock.StaticClock;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestFeature;

public class WindowManagerUnitTest {
	
	private static final Logger LOG = LoggerFactory.getLogger(WindowManagerUnitTest.class);
	
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
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 96),//723
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class2")), 25),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class6")), 39)
	);
	
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> TEST_PRIOR_EVENTS_6 = Arrays.asList(
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class3")), 62),
			new NaiveBayesCounts<NaiveBayesPriorProperty>(new NaiveBayesPriorProperty(new TestClassification("class4")), 74)
	);
	
	@SuppressWarnings("unchecked")
	private static final Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>>[] ALL_POSTERIOR_EVENTS = new Iterable[]{
		TEST_POSTERIOR_EVENTS_1, TEST_POSTERIOR_EVENTS_2, TEST_POSTERIOR_EVENTS_3, TEST_POSTERIOR_EVENTS_4, TEST_POSTERIOR_EVENTS_5, TEST_POSTERIOR_EVENTS_6
	};
	
	@SuppressWarnings("unchecked")
	private static final Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>>[] ALL_PRIOR_EVENTS = new Iterable[]{
		TEST_PRIOR_EVENTS_1, TEST_PRIOR_EVENTS_2, TEST_PRIOR_EVENTS_3, TEST_PRIOR_EVENTS_4, TEST_PRIOR_EVENTS_5, TEST_PRIOR_EVENTS_6
	};

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
	public void givenCandidate_whenAddingSingleEventSet_thenEventsAddedToWindow(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1), WindowUpdatedListener.NO_OP_LISTENER); // TODO update listener to be a mocked that captures invocation and verifies it got called
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.25)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.75)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(0.5)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.5)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));

		
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.371900826446281)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.628099173553719)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
	}
	
	@Test
	public void givenCandidate_whenAddingTwoEventSets_thenCorrectProbabilitiesCalculated(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 1000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2), WindowUpdatedListener.NO_OP_LISTENER);
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.2)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.60)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(0.2)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.9583333333333334)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(0.041666666666666664)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));

		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.29004329004329005)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.645021645021645)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(0.047619047619047616)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(0.017316017316017316)));
	}
	
	@Test
	public void givenCandidate_whenAddingThreeEventSets_thenCorrectProbabilitiesCalculated(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 1000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 2000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3), WindowUpdatedListener.NO_OP_LISTENER);
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.10714285714285714)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.8333333333333334)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(0.05952380952380952)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.9583333333333334)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(0.041666666666666664)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(0.8)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(0.2)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.2597402597402597)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.38701298701298705)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(0.17402597402597403)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(0.16883116883116883)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(0.01038961038961039)));
	}
	
	@Test
	public void givenCandidate_whenAddingMultipleEventsInSameWindowPeriod_thenEventsAddedToSameWindow(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 1000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 2000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 3000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_4, TEST_PRIOR_EVENTS_4), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 4000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_5, TEST_PRIOR_EVENTS_5), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 5000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_6, TEST_PRIOR_EVENTS_6), WindowUpdatedListener.NO_OP_LISTENER); // should not pop events1 off the buffer
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.06040268456375839)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.4697986577181208)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(0.0738255033557047)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.9712230215827338)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(0.02877697841726619)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(0.2206896551724138)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(0.16551724137931034)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(0.6140350877192983)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(1.0)));

		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.1362559241706161)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.20616113744075829)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(0.26540284360189575)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(0.3412322274881517)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class6")), is(equalTo(0.0462085308056872)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class7")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(0.004739336492890996)));
	}
	
	@Test
	public void givenCandidate_whenAddingMultipleEventsInDifferntWindowPeriodsWithinBuffer_thenEventsNotTruncated(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 1000);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 2 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_4, TEST_PRIOR_EVENTS_4), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 3 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_5, TEST_PRIOR_EVENTS_5), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + 4 * TEST_WINDOW_PERIOD);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_6, TEST_PRIOR_EVENTS_6), WindowUpdatedListener.NO_OP_LISTENER); // should not pop events1 off the buffer
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.06040268456375839)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.4697986577181208)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(0.0738255033557047)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(0.9712230215827338)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(0.02877697841726619)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(0.2206896551724138)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(0.16551724137931034)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(0.6140350877192983)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(1.0)));

		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.1362559241706161)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.20616113744075829)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(0.26540284360189575)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(0.3412322274881517)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class6")), is(equalTo(0.0462085308056872)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class7")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(0.004739336492890996)));
	}
	
	@Test
	public void givenCandidate_whenAddingMultipleEventsInDifferntWindowPeriodsOutsideOfBuffer_thenEventsTruncated(){
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_1, TEST_PRIOR_EVENTS_1), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + (TEST_WINDOW_PERIOD)+1);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + (2 *TEST_WINDOW_PERIOD)+1);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_3, TEST_PRIOR_EVENTS_3), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + (3 * TEST_WINDOW_PERIOD)+1);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_4, TEST_PRIOR_EVENTS_4), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + (4 * TEST_WINDOW_PERIOD)+1);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_5, TEST_PRIOR_EVENTS_5), WindowUpdatedListener.NO_OP_LISTENER);
		
		testClock.setCurrentTime(TEST_START_TIME + (5 * TEST_WINDOW_PERIOD)+1);
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_6, TEST_PRIOR_EVENTS_6), WindowUpdatedListener.NO_OP_LISTENER); // should not pop events1 off the buffer
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.031007751937984496)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.4263565891472868)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(0.08527131782945736)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(0.2206896551724138)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(0.16551724137931034)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(0.6140350877192983)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(1.0)));

		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.09681881051175657)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.1355463347164592)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(0.30982019363762103)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(0.3983402489626556)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class6")), is(equalTo(0.05394190871369295)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class7")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(0.005532503457814661)));
	}
	
	@Test
	public void givenCandidate_whenAddingMultipleEventsMultipleTimesThroughBuffer_thenEventsReturned(){
		
		long time = TEST_START_TIME+1;
		
		for (int i = 0; i < 12; i++){
			candidate.addNewProvider(getTestEvents(ALL_POSTERIOR_EVENTS[i%ALL_POSTERIOR_EVENTS.length], ALL_PRIOR_EVENTS[i%ALL_PRIOR_EVENTS.length]), WindowUpdatedListener.NO_OP_LISTENER);
			
			LOG.info("current time: "+time);
			LOG.info(candidate.toString());
			
			time += (TEST_WINDOW_PERIOD);
			
			testClock.setCurrentTime(time);
		}
		
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class1")), is(equalTo(0.031007751937984496)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class1")), is(equalTo(0.4263565891472868)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature3"), new TestClassification("class1")), is(equalTo(0.08527131782945736)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class2")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class2")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature1"), new TestClassification("class4")), is(equalTo(0.2206896551724138)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class4")), is(equalTo(0.16551724137931034)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature2"), new TestClassification("class8")), is(equalTo(1.0)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature5"), new TestClassification("class3")), is(equalTo(0.6140350877192983)));
		assertThat(candidate.getProbabilities().getPosteriorProbability(new TestFeature("feature8"), new TestClassification("class6")), is(equalTo(1.0)));

		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class1")), is(equalTo(0.09681881051175657)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class2")), is(equalTo(0.1355463347164592)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class3")), is(equalTo(0.30982019363762103)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class4")), is(equalTo(0.3983402489626556)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class5")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class6")), is(equalTo(0.05394190871369295)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class7")), is(equalTo(NaiveBayesProbabilities.NOMINAL_PROBABILITY)));
		assertThat(candidate.getProbabilities().getPriorProbability(new TestClassification("class8")), is(equalTo(0.005532503457814661)));
	}
	
	@Test
	public void givenCandidate_whenAddingMultipleEventsOverWindowPeriod_thenAllExpiredWindowsRemoved(){
		long time = TEST_START_TIME+1;
		
		// fill buffer
		for (int i = 0; i < 12; i++){
			candidate.addNewProvider(getTestEvents(ALL_POSTERIOR_EVENTS[i%ALL_POSTERIOR_EVENTS.length], ALL_PRIOR_EVENTS[i%ALL_PRIOR_EVENTS.length]), WindowUpdatedListener.NO_OP_LISTENER);
			
			time += (TEST_WINDOW_PERIOD);
			
			testClock.setCurrentTime(time);
		}
		
		// now add event after *5 the window period (basically empty the buffer)
		
		testClock.setCurrentTime(time + (5 * TEST_WINDOW_PERIOD));
		
		candidate.addNewProvider(getTestEvents(TEST_POSTERIOR_EVENTS_2, TEST_PRIOR_EVENTS_2), WindowUpdatedListener.NO_OP_LISTENER);
		
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
