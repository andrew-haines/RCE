package com.haines.ml.rce.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.dyuproject.protostuff.Message;
import com.google.common.collect.ImmutableMap;
import com.google.common.math.DoubleMath;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
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
	
	private CsvDataSet dataSet = new CsvDataSet.ShuttleDataSet(Collections.<Integer>emptyList());
	private String testName;
	
	private final Iterable<Message<?>> trainingSet;
	private final Iterable<Message<?>> testSet;
	
	public ShuttlePerformanceTest() throws IOException{
		super(new DynamicClassLoader());
		
		trainingSet = loadEvents("adult.data.txt");
		testSet = loadEvents("adult.test.txt");
	}
	
	@Override
	public void before(){
		// override. Let tests start up the candidate directly to control how it initiates
		
		dataSet = new CsvDataSet.EarningsDataSet(Collections.<Integer>emptyList());
	}

	@Override
	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory() {
		return new FeatureHandlerRepositoryFactory() {
			
			@Override
			public <E extends ClassifiedEvent> HandlerRepository<E> create() {
				
				Map<Integer, FeatureHandler<E>> featureHandlers = new ImmutableMap.Builder<Integer, FeatureHandler<E>>()
						
																						//.put(1, new SequentialDistributionFeatureHandler<E>())
																						//.put(2, new SequentialDistributionFeatureHandler<E>())
																						//.put(3, new SequentialDistributionFeatureHandler<E>()) 
																						//.put(4, new SequentialDistributionFeatureHandler<E>())
																						//.put(5, new SequentialDistributionFeatureHandler<E>())
																						//.put(6, new SequentialDistributionFeatureHandler<E>()) 
																						//.put(7, new SequentialDistributionFeatureHandler<E>())
																						//.put(8, new SequentialDistributionFeatureHandler<E>()) 
																						//.put(9, new SequentialDistributionFeatureHandler<E>()) 
																						.build();
				
				Map<Integer, ClassificationHandler<E>> classificationHandlers = new ImmutableMap.Builder<Integer, ClassificationHandler<E>>().build();
				
				return HandlerRepository.create(featureHandlers, classificationHandlers);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Message<E>> Iterable<E> loadTrainingEvents() throws IOException {
		return (Iterable<E>)trainingSet;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Message<E> & ClassifiedEvent> Iterable<E> loadTestEvents() throws IOException {
		return (Iterable<E>)testSet;
	}
	
	private Iterable<Message<?>> loadEvents(String datafileLocation) throws IOException{
		return PerformanceTest.UTILS.loadEvents(datafileLocation, true, ',', dataSet);
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllDiscreteData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		this.dataSet = new CsvDataSet.EarningsDataSet(Collections.<Integer>emptyList());
		this.testName = "discrete";
		super.before(); // perform the default setup
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAgeContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		Collection<Integer> featureTypes = Arrays.asList(1);
		
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		this.testName = "feature1";
		super.startUpRCE(getFeatureHandlerRepositoryFactory(featureTypes));
		
		Report report = testCurrentCandidate();
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.83", DoubleMath.fuzzyEquals(report.getAccuracy(), 0.83, 0.01) || report.getAccuracy() > 0.83, is(equalTo(true)));
	}
	
	@Test
	public void givenRCEApplicationConfiguredWithAllContinuousData_whenTrainedUsingShuttleDataSet_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException, RCEApplicationException, JAXBException {
		Collection<Integer> featureTypes = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
		
		this.dataSet = new CsvDataSet.EarningsDataSet(featureTypes);
		this.testName = "allContinuous";
		super.startUpRCE(getFeatureHandlerRepositoryFactory(featureTypes));
		
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
