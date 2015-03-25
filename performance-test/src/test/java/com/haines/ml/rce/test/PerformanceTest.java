package com.haines.ml.rce.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.dyuproject.protostuff.Message;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.service.ClassifierService;
import com.haines.ml.rce.service.ClassifierService.RandomisedClassifierService;
import com.haines.ml.rce.test.model.CsvDataSet;

public interface PerformanceTest {

	public static final Util UTILS = new Util();
	
	ClassifierService getClassifierService();

	void sendEvent(Event e);

	void notifyTrainingCompleted();

	void reset();
	
	public static class RandomPerformanceTest implements PerformanceTest {

		private final RandomisedClassifierService randomService;
		
		public RandomPerformanceTest(List<? extends Classification> possibleClassifications){
			this.randomService = new RandomisedClassifierService(possibleClassifications);
		}
		
		@Override
		public ClassifierService getClassifierService() {
			return randomService;
		}

		@Override
		public void sendEvent(Event e) {
			// no op
		}

		@Override
		public void notifyTrainingCompleted() {
			// no op
		}

		@Override
		public void reset() {
			// no op
		}
	}
	
	public static class Util {
		
		private Util(){}
		
		public Iterable<Message<?>> loadEvents(String dataFileLocation, boolean skipHeader, char delimiter, Function<CSVRecord, Message<?>> csvRecordConverter, String... headers) throws IOException {
			
			InputStream dataStream = DiscreteEarningsPerformanceTest.class.getResourceAsStream("/"+dataFileLocation);
			
			try(CSVParser parser = new CSVParser(new InputStreamReader(dataStream), CSVFormat.DEFAULT
																					.withSkipHeaderRecord(skipHeader)
																					.withHeader(headers
																		).withDelimiter(delimiter))){
			
				return Lists.newArrayList(Iterables.transform(parser, csvRecordConverter)); // load it all into memory so that we can close the underlying resource
			}
		}
		
		public Iterable<Message<?>> loadEvents(String dataFileLocation, boolean skipHeader, char delimiter, CsvDataSet dataSet) throws IOException{
			return loadEvents(dataFileLocation, skipHeader, delimiter, dataSet.getCSVConverterFunction(), Iterables.toArray(dataSet.getCsvHeaders(), String.class));
		}
	}
}
