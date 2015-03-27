package com.haines.ml.rce.test;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

import com.dyuproject.protostuff.Message;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.ReportGenerator.Report;
import com.haines.ml.rce.test.model.CsvDataSet;
import com.haines.ml.rce.test.model.DataSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class DiscreteEarningsPerformanceTest extends AbstractPerformanceTest {
	
	private final CsvDataSet dataSet = new CsvDataSet.EarningsDataSet(Collections.<Integer>emptyList());
	private final Iterable<ClassifiedEvent> trainingSet;
	private final Iterable<ClassifiedEvent> testSet;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected DiscreteEarningsPerformanceTest(ClassLoader classLoader) throws IOException {
		super(classLoader);
		
		trainingSet = (Iterable)Lists.newArrayList(loadEvents("adult.data.txt"));
		testSet = (Iterable)Lists.newArrayList(loadEvents("adult.test.txt"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public DiscreteEarningsPerformanceTest() throws IOException{
		super();
		
		trainingSet = (Iterable)Lists.newArrayList(loadEvents("adult.data.txt"));
		testSet = (Iterable)Lists.newArrayList(loadEvents("adult.test.txt"));
	}

	@Test
	public void givenRCEApplicationConfiguredWithDiscreteData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException {
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
		//assertThat(report.getFmeasure() > 0.88, is(equalTo(true)));
	}

	protected String getTestName() {
		return "discrete";
	}

	@Override
	protected Iterable<ClassifiedEvent> loadTrainingEvents() throws IOException {
		return trainingSet;
	}
	
	@Override
	protected Iterable<ClassifiedEvent> loadTestEvents() throws IOException {
		return testSet;
	}
	
	private Iterable<Message<?>> loadEvents(String datafileLocation) throws IOException{
		return PerformanceTest.UTILS.loadEvents(datafileLocation, true, ',', dataSet);
	}

	@Override
	protected DataSet getDataSet() {
		return dataSet;
	}
}
