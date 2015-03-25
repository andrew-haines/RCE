package com.haines.ml.rce.test.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVRecord;

import com.dyuproject.protostuff.Message;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.test.model.ContinuousTestEvent.DynamicFeature;
import com.haines.ml.rce.test.model.ContinuousTestEvent.IntegerFeature;
import com.haines.ml.rce.test.model.ContinuousTestEvent.StringFeature;
import com.haines.ml.rce.transport.Event.Classification;

public interface CsvDataSet extends DataSet{

	Function<CSVRecord, Message<?>> getCSVConverterFunction();
	
	Iterable<String> getCsvHeaders();
	
	public static class DataSetConvertor<F extends Feature> implements Function<CSVRecord, Message<?>>{

		private static final String CLASSIFICATION_COLUMN_NAME = "classification";
		
		private final DataConvertor<F> converter;
		private final Map<String, Integer> featureColumnNames;
		
		private DataSetConvertor(DataConvertor<F> converter, Map<String, Integer> featureColumnNames){
			this.converter = converter;
			this.featureColumnNames = featureColumnNames;
		}
		
		@Override
		public Message<?> apply(CSVRecord record) {
			
			List<F> features = new ArrayList<F>();
			
			for (Entry<String, Integer> featureColumnName: featureColumnNames.entrySet()){
				features.add(converter.getFeature(record.get(featureColumnName.getKey()), featureColumnName.getValue()));
			}
			
			return converter.createEvent(features, Arrays.asList(getClassification(record.get(CLASSIFICATION_COLUMN_NAME).trim())));
		}
		
		protected Classification getClassification(String value) {
			
			return new Classification(value);
		}
	}
	
	public static interface DataConvertor<F extends Feature> {
		
		Message<?> createEvent(List<F> features, List<Classification> classes);
		
		F getFeature(String value, int type);
		
		public static class DiscreteDataConvertor implements DataConvertor<com.haines.ml.rce.transport.Event.Feature> {

			private DiscreteDataConvertor(){}
			
			@Override
			public Message<?> createEvent(List<com.haines.ml.rce.transport.Event.Feature> features, List<Classification> classes) {
				com.haines.ml.rce.transport.Event event = new com.haines.ml.rce.transport.Event();
				
				event.setFeaturesList(features);
				event.setClassificationsList(classes);
				
				return event;
			}

			@Override
			public com.haines.ml.rce.transport.Event.Feature getFeature(String value, int type) {
				com.haines.ml.rce.transport.Event.Feature feature = new com.haines.ml.rce.transport.Event.Feature();
				
				feature.setType(type);
				feature.setValue(value);
				
				return feature;
			}
		}
		
		public static class ContinuousDataConvertor implements DataConvertor<ContinuousTestEvent.DynamicFeature<?>> {

			private final Collection<Integer> continuousTypes;
			
			private ContinuousDataConvertor(Collection<Integer> continuousTypes){
				this.continuousTypes = continuousTypes;
			}
			
			@Override
			public Message<?> createEvent(List<DynamicFeature<?>> features, List<Classification> classes) {
				
				ContinuousTestEvent event = new ContinuousTestEvent();
				
				event.setFeaturesList(features);
				event.setClassificationsList(classes);
				
				return event;
			}

			@Override
			public DynamicFeature<?> getFeature(String value, int type) {
				
				DynamicFeature<?> feature;
				
				if (continuousTypes.contains(type)){
					IntegerFeature tmp = new IntegerFeature();
					
					tmp.setValue(Integer.parseInt(value.trim()));
					
					feature = tmp;
					
				} else{
					StringFeature tmp = new StringFeature();
					
					tmp.setValue(value);
					
					feature = tmp;
				}
				
				feature.setType(type);
				
				return feature;
			}
			
		}
	}
	
	public static class EarningsDataSet implements CsvDataSet {
		
		private static final String POSITIVE_CLASS = "<=50K";
		private static final String NEGATIVE_CLASS = ">50K";

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
		
		private static final Map<String, Integer> FEATURES = ImmutableMap.<String, Integer>builder()
				
				.put(AGE_COLUMN_NAME, 1)
				.put(WORKCLASS_COLUMN_NAME, 2)
				.put(FNLWGT_COLUMN_NAME, 3)
				.put(EDUCATION_COLUMN_NAME, 4)
				.put(EDUCATION_NUM_COLUMN_NAME, 5)
				.put(MARITAL_STATUS_COLUMN_NAME, 6)
				.put(OCCUPATION_COLUMN_NAME, 7)
				.put(RELATIONSHIP_COLUMN_NAME, 8)
				.put(RACE_COLUMN_NAME, 9)
				.put(SEX_COLUMN_NAME, 10)
				.put(CAPITAL_GAIN_COLUMN_NAME, 11)
				.put(CAPITAL_LOSS_COLUMN_NAME, 12)
				.put(HOURS_PER_WEEK_COLUMN_NAME, 13)
				.put(NATIVE_COUNTRY_COLUMN_NAME, 14)
				.build();
		
		private static final DataSetConvertor<com.haines.ml.rce.transport.Event.Feature> DISCRETE_EARNINGS_CONVERTER = new DataSetConvertor<com.haines.ml.rce.transport.Event.Feature>(new DataConvertor.DiscreteDataConvertor(), FEATURES);
		
		private static final Iterable<String> CSV_HEADERS = Lists.newArrayList(
				AGE_COLUMN_NAME,
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
				DataSetConvertor.CLASSIFICATION_COLUMN_NAME);
		
		private Collection<Integer> continuousFeatureTypes;
		
		public EarningsDataSet(Collection<Integer> continuousFeatureTypes){
			this.continuousFeatureTypes = continuousFeatureTypes;
		}

		@Override
		public Iterable<String> getCsvHeaders() {
			return CSV_HEADERS;
		}

		@Override
		public Function<CSVRecord, Message<?>> getCSVConverterFunction() {
			if (Iterables.isEmpty(continuousFeatureTypes)){
				return DISCRETE_EARNINGS_CONVERTER;
			} else{
				return new DataSetConvertor<ContinuousTestEvent.DynamicFeature<?>>(new DataConvertor.ContinuousDataConvertor(continuousFeatureTypes), FEATURES);
			}
		}

		@Override
		public List<? extends Classification> getExpectedClasses() {
			return Arrays.asList(new Classification(POSITIVE_CLASS), new Classification(NEGATIVE_CLASS));
		}
	}
	
	public static class ShuttleDataSet implements CsvDataSet {

		private static final String FEATURE1 = "feature1";
		private static final String FEATURE2 = "feature2";
		private static final String FEATURE3 = "feature3";
		private static final String FEATURE4 = "feature4";
		private static final String FEATURE5 = "feature5";
		private static final String FEATURE6 = "feature6";
		private static final String FEATURE7 = "feature7";
		private static final String FEATURE8 = "feature8";
		private static final String FEATURE9 = "feature9";
		
		private static final Map<String, Integer> FEATURES = ImmutableMap.<String, Integer>builder()
				
				.put(FEATURE1, 1)
				.put(FEATURE2, 2)
				.put(FEATURE3, 3)
				.put(FEATURE4, 4)
				.put(FEATURE5, 5)
				.put(FEATURE6, 6)
				.put(FEATURE7, 7)
				.put(FEATURE8, 8)
				.put(FEATURE9, 9)
				.build();
		
		private static final Iterable<String> CSV_HEADERS = Arrays.asList(
				FEATURE1, 
				FEATURE2, 
				FEATURE3, 
				FEATURE4, 
				FEATURE5, 
				FEATURE6, 
				FEATURE7, 
				FEATURE8, 
				FEATURE9, 
				DataSetConvertor.CLASSIFICATION_COLUMN_NAME);
		
		private static final DataSetConvertor<com.haines.ml.rce.transport.Event.Feature> DISCRETE_EARNINGS_CONVERTER = new DataSetConvertor<com.haines.ml.rce.transport.Event.Feature>(new DataConvertor.DiscreteDataConvertor(), FEATURES);
		
		private Collection<Integer> continuousFeatureTypes;
		
		public ShuttleDataSet(Collection<Integer> continuousFeatureTypes){
			this.continuousFeatureTypes = continuousFeatureTypes;
		}
		
		@Override
		public List<? extends com.haines.ml.rce.model.Classification> getExpectedClasses() {
			return Arrays.asList(new Classification("1"), 
					new Classification("2"),
					new Classification("3"),
					new Classification("4"),
					new Classification("5"),
					new Classification("6"),
					new Classification("7"));
		}

		@Override
		public Function<CSVRecord, Message<?>> getCSVConverterFunction() {
			if (Iterables.isEmpty(continuousFeatureTypes)){
				return DISCRETE_EARNINGS_CONVERTER;
			} else{
				return new DataSetConvertor<ContinuousTestEvent.DynamicFeature<?>>(new DataConvertor.ContinuousDataConvertor(continuousFeatureTypes), FEATURES);
			}
		}

		@Override
		public Iterable<String> getCsvHeaders() {
			return CSV_HEADERS;
		}
	}
}
