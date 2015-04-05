package com.haines.ml.rce.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.ReportGenerator.Report;
import com.haines.ml.rce.test.data.SyntheticTestDataset;
import com.haines.ml.rce.test.model.DataSet;

public class SyntheticDataPerformanceTest extends AbstractPerformanceTest{

	private final SyntheticTestDataset dataset;
	private String testName;
	private Collection<Integer> continuousFeatureTypes = Collections.emptyList();
	
	public SyntheticDataPerformanceTest(){
		this.dataset = new SyntheticTestDataset(2, 5, 1.0);
	}
	
	@Override
	public void before() throws InterruptedException, RCEApplicationException, JAXBException, IOException{
		
		super.startUpRCE(getFeatureHandlerRepositoryFactory(continuousFeatureTypes));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllContinuousFeaturesData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		this.testName = "SyntheticData_continuous";
		this.continuousFeatureTypes = Lists.newArrayList(0, 1, 2, 3, 4, 5);
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllDiscreteFeaturesData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		this.testName = "SyntheticData_dicrete";
		this.continuousFeatureTypes = Collections.emptyList();
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Override
	protected String getTestName() {
		return testName;
	}

	@Override
	protected Iterable<ClassifiedEvent> loadTrainingEvents() throws IOException {
		return dataset.getEventsFromDistribution(20000);
	}

	@Override
	protected Iterable<ClassifiedEvent> loadTestEvents() throws IOException {
		return dataset.getEventsFromDistribution(1000);
	}

	@Override
	protected DataSet getDataSet() {
		return dataset;
	}
}
