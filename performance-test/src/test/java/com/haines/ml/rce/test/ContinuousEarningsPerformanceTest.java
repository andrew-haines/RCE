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
 * Tests the performance of using continuous distribution (normal) features. The results are as follows:
 * <code>
 * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 * |Age | Cap Gains | Cap Loss | Hour per Week |     tp     |     tn     |     fp     |     fn     |    total    |       Accuracy       |       f-measure      |   Memory Used   | Max Glob Accumulator Idx |
 * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 * | X  |    X      |    X     |       X       |   11460    |    2060    |    1786    |     975    |    16281    | 0.82943308150605     |  0.891722228720708   |      13 MB      |          129             |
 * | X  |    X      |    X     |               |   11449    |    2093    |    1753    |     986    |    16281    | 0.8317670904735581   |  0.8931622264695557  |      15 MB      |          159             |
 * | X  |    X      |          |               |   11582    |    2187    |    1659    |     853    |    16281    | 0.8457097229899884   |  0.9021654463312042  |      16 MB      |          170             |
 * | X  |           |          |               |   10674    |    2952    |    894     |     853    |    16281    | 0.8369264787175235   |  0.8893888263967004  |      17 MB      |          170             |
 * |    |    X      |          |               |   11570    |    2219    |    1627    |     865    |    16281    | 0.8469381487623611   |  0.9027777777777778  |      15 MB      |          170             |
 * |    |           |          |               |   10632    |    3023    |    823     |     1803   |    16281    | 0.8387076960874639   |  0.890079531184596   |      18 MB      |          229             |
 * ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 * </code>
 * 
 * @author haines
 *
 */
public class ContinuousEarningsPerformanceTest extends AbstractPerformanceTest {
	
	private CsvDataSet dataSet;
	private String testName;
	private Collection<Integer> featureTypes = Collections.<Integer>emptyList();
	
	public ContinuousEarningsPerformanceTest() throws IOException {
		super(new DynamicClassLoader());
	}
	
	public void before() throws InterruptedException, RCEApplicationException, JAXBException, IOException{
		this.startUpRCE(getFeatureHandlerRepositoryFactory());
		if (featureTypes.isEmpty()){
			dataSet = new CsvDataSet.EarningsDataSet(Collections.<Integer>emptyList());
			
			super.startUpRCE(getFeatureHandlerRepositoryFactory());
		} else{
			dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		
			super.startUpRCE(getFeatureHandlerRepositoryFactory(featureTypes));
		}
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllDiscreteData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		this.testName = "discrete";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAgeContinuousData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(1);
		
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
	
		this.testName = "ageC";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithCapGainsContinuousData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(11);
		
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		
		this.testName = "capGainC";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithCapLossContinuousData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(12);
		
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		
		this.testName = "capLossC";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithHoursPerWeekContinuousData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(13);
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		
		this.testName = "hrPerWkC";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllContinuousData_whenTrainedUsingEarningDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		featureTypes = Arrays.asList(1, 11, 12, 13);
		
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		
		this.testName = "allContinuous";
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}

	protected String getTestName() {
		return testName;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Iterable<ClassifiedEvent> loadTrainingEvents() throws IOException {
		return (Iterable)loadEvents("adult.data.txt");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected Iterable<ClassifiedEvent> loadTestEvents() throws IOException {
		return (Iterable)loadEvents("adult.test.txt");
	}

	@Override
	protected DataSet getDataSet() {
		return dataSet;
	}
	
	private Iterable<Message<?>> loadEvents(String datafileLocation) throws IOException{
		return PerformanceTest.UTILS.loadEvents(datafileLocation, true, ',', dataSet);
	}
}
