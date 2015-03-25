package com.haines.ml.rce.test;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.Message;
import com.google.common.collect.Lists;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.main.RCEApplicationStartupTest;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.test.ReportGenerator.Report;
import com.haines.ml.rce.transport.Event;
import com.haines.ml.rce.transport.Event.Classification;
import com.haines.ml.rce.transport.Event.Feature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class DiscretePerformanceTest extends RCEApplicationStartupTest implements PerformanceTest{

	private static final Logger LOG = LoggerFactory.getLogger(DiscretePerformanceTest.class);

	private static final String POSITIVE_CLASS = "<=50K";
	private static final String NEGATIVE_CLASS = ">50K";

	protected static final String CLASSIFICATION_COLUMN_NAME = "classification";
	protected static final String AGE_COLUMN_NAME = "age";
	protected static final String WORKCLASS_COLUMN_NAME = "workclass";
	protected static final String FNLWGT_COLUMN_NAME = "fnlwgt";
	protected static final String EDUCATION_COLUMN_NAME = "education";
	protected static final String EDUCATION_NUM_COLUMN_NAME = "education-num";
	protected static final String MARITAL_STATUS_COLUMN_NAME = "marital-status";
	protected static final String OCCUPATION_COLUMN_NAME = "occupation";
	protected static final String RELATIONSHIP_COLUMN_NAME = "relationship";
	protected static final String RACE_COLUMN_NAME = "race";
	protected static final String SEX_COLUMN_NAME = "sex";
	protected static final String CAPITAL_GAIN_COLUMN_NAME = "capital-gain";
	protected static final String CAPITAL_LOSS_COLUMN_NAME = "capital-loss";
	protected static final String HOURS_PER_WEEK_COLUMN_NAME = "hours-per-week";
	protected static final String NATIVE_COUNTRY_COLUMN_NAME = "native-country";
	
	protected DiscretePerformanceTest(ClassLoader classLoader) {
		super(classLoader);
	}
	
	public DiscretePerformanceTest(){
		super();
	}

	@Test
	public void givenRCEApplication_whenTrained_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException {
		
		String existingThreadName = Thread.currentThread().getName();
		
		Thread.currentThread().setName(existingThreadName+" - "+getTestName());
		LOG.info("Loading required data...");
		
		Iterable<? extends ClassifiedEvent> trainingEvents = loadTrainingEvents();
		
		Iterable<? extends ClassifiedEvent> testingEvents = loadTestEvents();
		
		LOG.info("Finished loading required data. Starting tests");
		
		Report report = new ReportGenerator(getTestName(), 2, 200, this).getReport(trainingEvents, testingEvents, getExpectedClasses());
		
		Report randomReport = new ReportGenerator("Random", 2, 100, new RandomPerformanceTest(getExpectedClasses())).getReport(trainingEvents, testingEvents, getExpectedClasses());
		
		getReportRenderer().render(Lists.newArrayList(report, randomReport));
		
		if (System.getProperty("waitForKeyInput") != null){
			System.in.read();
		}
		
		assertThat("Accuracy "+report.getAccuracy()+" is not above 0.82", report.getAccuracy() > 0.82, is(equalTo(true)));
		//assertThat(report.getFmeasure() > 0.88, is(equalTo(true)));
		
		Thread.currentThread().setName(existingThreadName);
	}

	private ReportRenderer getReportRenderer() {
		ReportRenderer renderer = new ReportRenderer.SLF4JReportRenderer();
		
		if (!GraphicsEnvironment.isHeadless()){
			renderer = ReportRenderer.UTIL.chain(renderer, new ReportRenderer.JPanelJChartROCRenderer());
		}
		
		return renderer;
		
	}

	protected List<? extends com.haines.ml.rce.model.Classification> getExpectedClasses() {
		return Lists.newArrayList(getClassification(POSITIVE_CLASS), getClassification(NEGATIVE_CLASS));
	}
	
	private Classification getClassification(String classification){
		return new Classification(classification);
	}

	protected String getTestName() {
		return "discrete";
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
	public void givenCandidate_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly(){
		// overload to disable inheriting test
	}

	protected <E extends Message<E>> Iterable<E> loadTrainingEvents() throws IOException {
		return loadEvents("adult.data.txt");
	}
	
	protected <E extends Message<E> & ClassifiedEvent> Iterable<E> loadTestEvents() throws IOException {
		return loadEvents("adult.test.txt");
	}
	
	private <E extends Message<E>> Iterable<E> loadEvents(String datafileLocation) throws IOException{
		return loadEvents(datafileLocation, true, ',', AGE_COLUMN_NAME,
				WORKCLASS_COLUMN_NAME,
				FNLWGT_COLUMN_NAME,
				EDUCATION_COLUMN_NAME,
				EDUCATION_NUM_COLUMN_NAME, 
				MARITAL_STATUS_COLUMN_NAME,
				OCCUPATION_COLUMN_NAME, 
				RELATIONSHIP_COLUMN_NAME,
				RACE_COLUMN_NAME,
				SEX_COLUMN_NAME,
				CAPITAL_GAIN_COLUMN_NAME,
				CAPITAL_LOSS_COLUMN_NAME,
				HOURS_PER_WEEK_COLUMN_NAME,
				NATIVE_COUNTRY_COLUMN_NAME,
				CLASSIFICATION_COLUMN_NAME);
	}

	protected <E extends Message<E>> Iterable<E> loadEvents(String dataFileLocation, boolean skipHeader, char delimiter, String... headers) throws IOException {
		
		InputStream dataStream = DiscretePerformanceTest.class.getResourceAsStream("/"+dataFileLocation);
		
		try(CSVParser parser = new CSVParser(new InputStreamReader(dataStream), CSVFormat.DEFAULT
																				.withSkipHeaderRecord(skipHeader)
																				.withHeader(headers
																	).withDelimiter(delimiter))){
		
			Collection<E> events = new ArrayList<E>();
			
			for (CSVRecord record: parser){
				
				addCSVRecordToEvents(record, events);
			}
			return events;
		}
	}

	@SuppressWarnings("unchecked")
	protected <E extends Message<E>> void addCSVRecordToEvents(CSVRecord record, Collection<E> events) {
		Event event = new Event();
		
		List<Feature> features = new ArrayList<Feature>();
		
		features.add(getFeature(record.get(AGE_COLUMN_NAME), 1));
		features.add(getFeature(record.get(WORKCLASS_COLUMN_NAME), 2));
		features.add(getFeature(record.get(FNLWGT_COLUMN_NAME), 3));
		features.add(getFeature(record.get(EDUCATION_COLUMN_NAME), 4));
		features.add(getFeature(record.get(EDUCATION_NUM_COLUMN_NAME), 5));
		features.add(getFeature(record.get(MARITAL_STATUS_COLUMN_NAME), 6));
		features.add(getFeature(record.get(OCCUPATION_COLUMN_NAME), 7));
		features.add(getFeature(record.get(RELATIONSHIP_COLUMN_NAME), 8));
		features.add(getFeature(record.get(RACE_COLUMN_NAME), 9));
		features.add(getFeature(record.get(SEX_COLUMN_NAME), 10));
		features.add(getFeature(record.get(CAPITAL_GAIN_COLUMN_NAME), 11));
		features.add(getFeature(record.get(CAPITAL_LOSS_COLUMN_NAME), 12));
		features.add(getFeature(record.get(HOURS_PER_WEEK_COLUMN_NAME), 13));
		features.add(getFeature(record.get(NATIVE_COUNTRY_COLUMN_NAME), 14));
		
		event.setFeaturesList(features);
		event.setClassificationsList(Arrays.asList(getClassification(record)));
		
		events.add((E)event);
	}
	
	private Feature getFeature(String value, int type){
		Feature feature = new Feature();
		
		feature.setValue(value.trim());
		feature.setType(type);
		
		return feature;
	}

	protected Classification getClassification(CSVRecord record) {
		Classification classification = new Classification();
		
		classification.setValue(record.get(CLASSIFICATION_COLUMN_NAME).trim());
		return classification;
	}

	@Override
	public NaiveBayesService getClassifierService() {
		return super.candidate.getNaiveBayesService();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void sendEvent(com.haines.ml.rce.model.Event event) {
		try {
			super.sendViaSelector((Message)event);
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException("Unable to send event "+event+" to selector ", e);
		}
	}

	@Override
	public void notifyTrainingCompleted() {
		
		// pause the test to ensure that the training events will propagate through to the model
		waitingForNextWindow.set(true);
		
		try {
			super.nextWindowUpdated.await();
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			throw new RuntimeException("Unable to wait for system", e);
		}
	}

	@Override
	public void reset() {
		try {
			super.after();
			super.before();
		} catch (RCEApplicationException | InterruptedException | JAXBException | IOException e) {
			throw new RuntimeException("Unable to stop existing service", e);
		}
		
	}
	
}
