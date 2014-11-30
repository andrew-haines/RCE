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

import com.haines.ml.rce.main.RCEApplicationStartupTest;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.transport.Event;
import com.haines.ml.rce.transport.Event.Classification;
import com.haines.ml.rce.transport.Event.Feature;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class PerformanceTest extends RCEApplicationStartupTest{
	
	private static final Logger LOG = LoggerFactory.getLogger(PerformanceTest.class);

	private static final String POSITIVE_CLASS = "<=50K";
	private static final String NEGATIVE_CLASS = ">50K";

	private static final String CLASSIFICATION_COLUMN_NAME = "classification";
	private static final String AGE_COLUMN_NAME = "age";
	private static final String WORKCLASS_COLUMN_NAME = "workclass";
	private static final String FNLWGT_COLUMN_NAME = "fnlwgt";
	private static final String EDUCATION_COLUMN_NAME = "education";
	private static final String EDUCATION_NUM_COLUMN_NAME = "education-num";
	private static final String MARITAL_STATUS_COLUMN_NAME = "marital-status";
	private static final String OCCUPATION_COLUMN_NAME = "occupation";
	private static final String RELATIONSHIP_COLUMN_NAME = "relationship";
	private static final String RACE_COLUMN_NAME = "race";
	private static final String SEX_COLUMN_NAME = "sex";
	private static final String CAPITAL_GAIN_COLUMN_NAME = "capital-gain";
	private static final String CAPITAL_LOSS_COLUMN_NAME = "capital-loss";
	private static final String HOURS_PER_WEEK_COLUMN_NAME = "hours-per-week";
	private static final String NATIVE_COUNTRY_COLUMN_NAME = "native-country";

	@Test
	public void givenRCEApplication_whenTrained_thenGetAndReportClassifierPerformance() throws IOException, InterruptedException {
		Iterable<Event> trainingEvents = loadTrainingEvents();
		
		for (Event event: trainingEvents){
			super.sendViaSelector(event);
		}
		
		waitingForNextWindow.set(true);
		
		Thread.sleep(5000);
		
		NaiveBayesService classifierService = super.candidate.getNaiveBayesService();
		
		Iterable<Event> testingEvents = loadTestEvents();
		
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;
		int total = 0;
		
		for (Event event: testingEvents){
			total++;
			
			com.haines.ml.rce.model.Classification classification = classifierService.getMaximumLikelihoodClassification(event.getFeaturesList());
			
			if (classification.getValue().equals(event.getClassificationsList().get(0).getValue())){
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
		
		LOG.info("classifier results: tp="+tp+" tn="+tn+" fp="+fp+" fn="+fn+" total="+total);
	}

	private Iterable<Event> loadTrainingEvents() throws IOException {
		return loadEvents("adult.data.txt");
	}
	
	private Iterable<Event> loadTestEvents() throws IOException {
		return loadEvents("adult.test.txt");
	}

	private Iterable<Event> loadEvents(String dataFileLocation) throws IOException {
		
		InputStream dataStream = PerformanceTest.class.getResourceAsStream("/"+dataFileLocation);
		
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
		
			Collection<Event> events = new ArrayList<Event>();
			
			for (CSVRecord record: parser){
				
				events.add(convertCSVRecordToEvent(record));
			}
			return events;
		}
	}

	private Event convertCSVRecordToEvent(CSVRecord record) {
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
		
		return event;
	}
	
	private Feature getFeature(String value, int type){
		Feature feature = new Feature();
		
		feature.setValue(value.trim());
		feature.setType(type);
		
		return feature;
	}

	private Classification getClassification(CSVRecord record) {
		Classification classification = new Classification();
		
		classification.setValue(record.get(CLASSIFICATION_COLUMN_NAME).trim());
		return classification;
	}
	
}
