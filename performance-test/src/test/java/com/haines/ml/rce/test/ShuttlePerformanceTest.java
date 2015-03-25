package com.haines.ml.rce.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.dyuproject.protostuff.Message;
import com.google.common.collect.ImmutableMap;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.accumulator.handlers.SequentialDistributionFeatureHandler;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.ContinuousPerformanceTest.ContiuousTestEvent.DynamicFeature;

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
public class ShuttlePerformanceTest extends ContinuousPerformanceTest {

	private static final String FEATURE1 = "feature1";
	private static final String FEATURE2 = "feature2";
	private static final String FEATURE3 = "feature3";
	private static final String FEATURE4 = "feature4";
	private static final String FEATURE5 = "feature5";
	private static final String FEATURE6 = "feature6";
	private static final String FEATURE7 = "feature7";
	private static final String FEATURE8 = "feature8";
	private static final String FEATURE9 = "feature9";

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
	
	@Override
	protected <E extends Message<E>> Iterable<E> loadTrainingEvents() throws IOException {
		return loadEvents("shuttle-train", false, ' ', FEATURE1, FEATURE2, FEATURE3, FEATURE4, FEATURE5, FEATURE6, FEATURE7, FEATURE8, FEATURE9, CLASSIFICATION_COLUMN_NAME);
	}
	
	@Override
	protected <E extends Message<E> & ClassifiedEvent> Iterable<E> loadTestEvents() throws IOException {
		return loadEvents("shuttle-test", false, ' ', FEATURE1, FEATURE2, FEATURE3, FEATURE4, FEATURE5, FEATURE6, FEATURE7, FEATURE8, FEATURE9, CLASSIFICATION_COLUMN_NAME);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Message<E>> void addCSVRecordToEvents(CSVRecord record, Collection<E> events) {
		
		ContiuousTestEvent event = new ContiuousTestEvent();
		
		List<DynamicFeature<?>> features = new ArrayList<DynamicFeature<?>>();
		
		features.add(getFeature(record.get(FEATURE1), 1));
		features.add(getFeature(record.get(FEATURE2), 2));
		features.add(getFeature(record.get(FEATURE3), 3));
		features.add(getFeature(record.get(FEATURE4), 4));
		features.add(getFeature(record.get(FEATURE5), 5));
		features.add(getFeature(record.get(FEATURE6), 6));
		features.add(getFeature(record.get(FEATURE7), 7));
		features.add(getFeature(record.get(FEATURE8), 8));
		features.add(getFeature(record.get(FEATURE9), 9));
		
		event.setFeaturesList(features);
		event.setClassificationsList(Arrays.asList(getClassification(record)));
		
		events.add((E)event);
	}
	
	@Override
	protected DynamicFeature<?> getFeature(String featureValue, int type){
		featureValue = featureValue.trim();
		
		ContiuousTestEvent.IntegerFeature feature = new ContiuousTestEvent.IntegerFeature();
		
		feature.setValue(Integer.parseInt(featureValue));
		feature.setType(type);
		
		return feature;
	}

	@Override
	protected String getTestName() {
		return "Shuttle";
	}
}
