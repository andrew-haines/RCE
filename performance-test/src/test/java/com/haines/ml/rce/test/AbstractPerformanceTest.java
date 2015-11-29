package com.haines.ml.rce.test;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.accumulator.handlers.SequentialDistributionFeatureHandler;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.main.RCEApplicationStartupTest;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.test.ReportGenerator.Report;
import com.haines.ml.rce.test.model.DataSet;

public abstract class AbstractPerformanceTest extends RCEApplicationStartupTest implements PerformanceTest{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractPerformanceTest.class);
	
	protected static final Collection<Report> GENERATED_REPORTS = new ArrayList<Report>();
	
	protected AbstractPerformanceTest(ClassLoader classLoader){
		super(classLoader);
	}
	
	@AfterClass
	public static void afterClass() throws IOException{
		getReportRenderer().render(GENERATED_REPORTS);
		
		if (System.getProperty("waitForKeyInput") != null){
			System.in.read();
		}
	}
	
	private static ReportRenderer getReportRenderer() {
		ReportRenderer renderer = new ReportRenderer.SLF4JReportRenderer();
		
		if (!GraphicsEnvironment.isHeadless()){
			renderer = ReportRenderer.UTIL.chain(renderer, new ReportRenderer.JPanelJChartROCRenderer());
		}
		
		return renderer;
		
	}
	
	@Test // technically not a test but it runs a random classifier through the report generation process
	public void addRandomReport() throws IOException{
		
		Iterable<? extends ClassifiedEvent> trainingEvents = loadTrainingEvents();
		
		Iterable<? extends ClassifiedEvent> testingEvents = loadTestEvents();
		
		Report randomReport = new ReportGenerator("Random", 2, 100, new RandomPerformanceTest(getDataSet().getExpectedClasses())).getReport(trainingEvents, testingEvents, getDataSet().getExpectedClasses());
		
		GENERATED_REPORTS.add(randomReport);
	}
	
	protected AbstractPerformanceTest(){
		super();
	}
	
	@Override
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticData_thenClassifierWorksAsExpected(){
		// overload to disable inheriting test
	}
	
	@Override
	public void givenCandidate_whenCallingStart_thenApplicationStartsUpCorrectly(){
		// overload to disable inheriting test
	}
	
	@Override
	public void givenCandidateAndTCPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly(){
		// overload to disable inheriting test
	}
	
	@Override
	public void givenCandidateAndUDPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly(){
		// overload to disable inheriting test
	}
	
	@Override
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticDataOverMultipleWindows_thenClassifierWorksAsExpected(){
		// overload to disable inheriting test
	}
	
	@Override
	protected boolean isUsingSlf4jEventListener(){
		return false;
	}
	
	@Override
	public void sendEvent(com.haines.ml.rce.model.Event event) {
		candidate.getEventConsumer().consume(event);
	}

	@Override
	public void notifyTrainingCompleted() {
		
		// pause the test to ensure that the training events will propagate through to the model
		
		try {
			Thread.sleep(6000);
			
			waitingForNextWindow.set(true);
			super.nextWindowUpdated.await(5, TimeUnit.SECONDS);
			
		} catch (InterruptedException e) {
			throw new RuntimeException("Unable to wait for system", e);
		}
	}

	@Override
	public void reset() {
		try {
			this.after();
			this.startUpRCE(getFeatureHandlerRepositoryFactory());
		} catch (RCEApplicationException | InterruptedException | JAXBException | IOException e) {
			throw new RuntimeException("Unable to stop existing service", e);
		}
	}
	
	@Override
	public NaiveBayesService getClassifierService() {
		return super.candidate.getNaiveBayesService();
	}
	
	protected abstract boolean isUsingNFoldValidation();
	
	protected Report testCurrentCandidate() throws IOException{
		String existingThreadName = Thread.currentThread().getName();
		
		Thread.currentThread().setName(existingThreadName+" - "+getTestName());
		LOG.info("Loading required data...");
		
		Iterable<? extends ClassifiedEvent> trainingEvents = loadTrainingEvents();
		
		Iterable<? extends ClassifiedEvent> testingEvents = loadTestEvents();
		
		LOG.info("Finished loading required data. Starting tests");
		
		ReportGenerator reportGenerator = new ReportGenerator(getTestName(), 3, 200, this);
		try{
			
			Report report;
			if (isUsingNFoldValidation()){
				report = reportGenerator.getReport(Lists.newArrayList(Iterables.concat(trainingEvents, testingEvents)), 5, getDataSet().getExpectedClasses());
			}else {
				report = reportGenerator.getReport(trainingEvents, testingEvents, getDataSet().getExpectedClasses());
			}
			GENERATED_REPORTS.add(report);
			
			return report;
		} finally {
			Thread.currentThread().setName(existingThreadName);
		}
	}
	
	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory(final Iterable<Integer> continuousFeatureTypes) {
		return new FeatureHandlerRepositoryFactory() {
			
			@Override
			public <E extends ClassifiedEvent> HandlerRepository<E> create() {
				
				ImmutableMap.Builder<Integer, FeatureHandler<E>> featureHandlers = new ImmutableMap.Builder<Integer, FeatureHandler<E>>();
				
				for (Integer featureType: continuousFeatureTypes){
					featureHandlers.put(featureType, new SequentialDistributionFeatureHandler<E>());
				}
				Map<Integer, ClassificationHandler<E>> classificationHandlers = new ImmutableMap.Builder<Integer, ClassificationHandler<E>>().build();
				
				return HandlerRepository.create(featureHandlers.build(), classificationHandlers);
			}
		};
	}
	
	protected abstract String getTestName();
	
	protected abstract Iterable<ClassifiedEvent> loadTrainingEvents() throws IOException;
	
	protected abstract Iterable<ClassifiedEvent> loadTestEvents() throws IOException;
	
	protected abstract DataSet getDataSet();
}
