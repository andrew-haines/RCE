package com.haines.ml.rce.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dyuproject.protostuff.Message;
import com.google.common.collect.Iterables;
import com.haines.ml.rce.main.RCEApplicationStartupTest;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.transport.Event;
import com.haines.ml.rce.transport.Event.Classification;
import com.haines.ml.rce.transport.Event.Feature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class DiscretePerformanceTest extends RCEApplicationStartupTest{

	private static final Logger LOG = LoggerFactory.getLogger(DiscretePerformanceTest.class);

	private static final String POSITIVE_CLASS = "<=50K";
	private static final String NEGATIVE_CLASS = ">50K";

	private static final String CLASSIFICATION_COLUMN_NAME = "classification";
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

	@SuppressWarnings("unchecked")
	@Test
	public void givenRCEApplication_whenTrained_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException {
		
		long heapSize = getMemoryAfterGC();
		
		Iterable<? extends Message<?>> trainingEvents = loadTrainingEvents();
		
		// take a snapshot of memory usage before we send events to the system
		
		for (@SuppressWarnings("rawtypes") Message event: trainingEvents){
			super.sendViaSelector(event);
		}
		
		trainingEvents = null; // gc food. Most jvm's will do this automatically but this belts and braces
		
		waitingForNextWindow.set(true);
		
		super.nextWindowUpdated.await();
		Thread.sleep(6000);
		
		NaiveBayesService classifierService = super.candidate.getNaiveBayesService();
		
		Iterable<? extends ClassifiedEvent> testingEvents = loadTestEvents();
		
		double tp = 0;
		double fp = 0;
		double tn = 0;
		double fn = 0;
		double total = 0;
		
		ClassifiedEvent firstEvent = null;
		
		for (ClassifiedEvent event: testingEvents){
			
			if (firstEvent == null){
				firstEvent = event;
			}
			total++;
			
			com.haines.ml.rce.model.Classification classification = classifierService.getMaximumLikelihoodClassification(event.getFeaturesList()).getClassification();
			
			if (classification.getValue().equals(Iterables.get(event.getClassificationsList(), 0).getValue())){
				if (classification.getValue().equals(POSITIVE_CLASS)){
					tp++;
				} else{
					tn++;
				}
			} else{
				if (classification.getValue().equals(POSITIVE_CLASS)){
					fp++;
				} else{
					fn++;
				}
			}
		}
		
		testingEvents = null; // gc food.
		
		long heapAfterTrainingSize = getMemoryAfterGC();
		
		LOG.info("classifier trained with: {} MB of memory used for model", ((heapAfterTrainingSize - heapSize) / (1024 * 1024)));
		
		LOG.info(getTestName()+":: classifier results: tp="+tp+" tn="+tn+" fp="+fp+" fn="+fn+" total="+total);
		
		double accuracy = ((tp+tn) / total);
		double fmeasure = (2*tp / (2*tp + fp + fn));
		LOG.info("classifier accuracy: "+accuracy);
		LOG.info("classifier fmeasure: "+fmeasure);
		
		classifierService.getMaximumLikelihoodClassification(firstEvent.getFeaturesList()); // in order to prevent JVM making model available for GC prior to obtaining memory usage above.
		
		assertThat(accuracy > 0.82, is(equalTo(true)));
		assertThat(fmeasure > 0.88, is(equalTo(true)));
	}
	
	private long getMemoryAfterGC() {
		
		Runtime rt = Runtime.getRuntime();
		
		rt.gc();
		
		long usedMemory = rt.totalMemory() - rt.freeMemory();
		
		return usedMemory;
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

	private <E extends Message<E>> Iterable<E> loadTrainingEvents() throws IOException {
		return loadEvents("adult.data.txt");
	}
	
	private <E extends Message<E> & ClassifiedEvent> Iterable<E> loadTestEvents() throws IOException {
		return loadEvents("adult.test.txt");
	}

	private <E extends Message<E>> Iterable<E> loadEvents(String dataFileLocation) throws IOException {
		
		InputStream dataStream = DiscretePerformanceTest.class.getResourceAsStream("/"+dataFileLocation);
		
		try(CSVParser parser = new CSVParser(new InputStreamReader(dataStream), CSVFormat.DEFAULT
																				.withSkipHeaderRecord(true)
																				.withHeader(AGE_COLUMN_NAME,
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
																							CLASSIFICATION_COLUMN_NAME
																	).withDelimiter(','))){
		
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
	
}
