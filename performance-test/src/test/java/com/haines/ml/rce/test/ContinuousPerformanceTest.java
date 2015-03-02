package com.haines.ml.rce.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.common.collect.ImmutableMap;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.accumulator.handlers.SequentialDistributionFeatureHandler;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;

public class ContinuousPerformanceTest extends DiscretePerformanceTest {

	@Override
	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory() {
		return new FeatureHandlerRepositoryFactory() {
			
			@Override
			public <E extends ClassifiedEvent> HandlerRepository<E> create() {
				
				Map<Integer, FeatureHandler<E>> featureHandlers = new ImmutableMap.Builder<Integer, FeatureHandler<E>>()
						
																						.put(1, new SequentialDistributionFeatureHandler<E>()) // set age as a continuous feature
																						.put(11, new SequentialDistributionFeatureHandler<E>()) // set capital gains recorded
																						.put(12, new SequentialDistributionFeatureHandler<E>()) // set capital loss recorded
																						.put(13, new SequentialDistributionFeatureHandler<E>()) // set hours per week
																						.build();
				
				Map<Integer, ClassificationHandler<E>> classificationHandlers = new ImmutableMap.Builder<Integer, ClassificationHandler<E>>().build();
				
				return HandlerRepository.create(featureHandlers, classificationHandlers);
			}
		};
	}

	@Override
	protected String getTestName() {
		return "Continuous";
	}

	@Override
	protected <E extends Message<E>> void addCSVRecordToEvents(CSVRecord record, Collection<E> events) {
		
		ContiuousTestEvent event = new ContiuousTestEvent();
		
		
		
		events.add((E)event);
	}
	
	public static class ContiuousTestEvent implements ClassifiedEvent, Message<ContiuousTestEvent>{
		
		private final static Schema<ContiuousTestEvent> SCHEMA = RuntimeSchema.getSchema(ContiuousTestEvent.class);

		private List<? extends Feature> featuresList; 
		private List<? extends Classification> classificationsList; 
		
		@Override
		public Collection<? extends Feature> getFeaturesList() {
			return featuresList;
		}

		@Override
		public Collection<? extends Classification> getClassificationsList() {
			return classificationsList;
		}

		@Override
		public Schema<ContiuousTestEvent> cachedSchema() {
			return SCHEMA;
		}
		
	}
}
