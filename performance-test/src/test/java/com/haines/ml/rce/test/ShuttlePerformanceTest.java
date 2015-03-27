package com.haines.ml.rce.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.dyuproject.protostuff.Message;
import com.google.common.math.DoubleMath;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.ReportGenerator.Report;
import com.haines.ml.rce.test.model.CsvDataSet;
import com.haines.ml.rce.test.model.DataSet;

/**
 * Using the shuttle dataset. 
 * The results are as follows:
 * <code>
 * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 * | Feature1 | Feature2 | Feature3 | Feature4 | Feature5 | Feature6 | Feature7 | Feature8 | Feature9 |     tp     |     tn     |     fp     |     fn     |    total    |       Accuracy       |       f-measure      |   Memory Used   | Max Glob Accumulator Idx |
 * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 * |          |          |          |          |          |          |          |          |          |   0        |    14453   |    1786    |     975    |    16281    | 0.82943308150605     |  0.891722228720708   |      13 MB      |          129             |
 * </code>
 * 
 * @author haines
 *
 */
public class ShuttlePerformanceTest extends AbstractPerformanceTest {

	private String testName;
	private Collection<Integer> featureTypes = Collections.<Integer>emptyList();
	private CsvDataSet dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
	
	public ShuttlePerformanceTest() throws IOException{
		super(new DynamicClassLoader());
	}
	
	@Override
	public void before() throws InterruptedException, RCEApplicationException, JAXBException, IOException{
		// override. Let tests start up the candidate directly to control how it initiates
		
		if (featureTypes.isEmpty()){
			dataSet = new CsvDataSet.ShuttleDataSet(Collections.<Integer>emptyList());
			
			super.startUpRCE(getFeatureHandlerRepositoryFactory());
		} else{
			dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		
			super.startUpRCE(getFeatureHandlerRepositoryFactory(featureTypes));
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Message<E>> Iterable<E> loadTrainingEvents() throws IOException {
		return (Iterable<E>)loadEvents("shuttle-train");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Message<E> & ClassifiedEvent> Iterable<E> loadTestEvents() throws IOException {
		return (Iterable<E>)loadEvents("shuttle-test");
	}
	
	private Iterable<Message<?>> loadEvents(String datafileLocation) throws IOException{
		return PerformanceTest.UTILS.loadEvents(datafileLocation, false, ' ', dataSet);
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllDiscreteData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		this.dataSet = new CsvDataSet.ShuttleDataSet(Collections.<Integer>emptyList());
		this.testName = "discrete";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature1ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(1);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature1";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature2ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(2);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature2";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature3ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(3);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature3";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature4ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(4);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature4";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature5ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(5);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature5";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature6ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(6);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature6";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature7ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(7);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature7";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature8ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(8);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature8";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithFeature9ContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(9);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "feature9";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
		
		this.dataSet = new CsvDataSet.ShuttleDataSet(featureTypes);
		this.testName = "allContinuous";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}

	@Override
	protected String getTestName() {
		return testName;
	}

	@Override
	protected DataSet getDataSet() {
		return dataSet;
	}
}
