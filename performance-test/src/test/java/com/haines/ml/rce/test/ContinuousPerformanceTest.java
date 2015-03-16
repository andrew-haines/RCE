package com.haines.ml.rce.test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
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
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.test.ContinuousPerformanceTest.ContiuousTestEvent.DynamicFeature;

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
public class ContinuousPerformanceTest extends DiscretePerformanceTest {

	public ContinuousPerformanceTest() {
		super(new ClassLoader(Thread.currentThread().getContextClassLoader()){

			@Override
			public Enumeration<URL> getResources(String name) throws IOException {
				
				if ("META-INF/services/com.haines.ml.rce.main.factory.RCEApplicationFactory".equals(name)){ // override the factory service file to use the dynamic protostuff factory
					name = name+"_dynamic"; 
				}
				return super.getResources(name);
			}
			
			
		});
	}

	@Override
	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory() {
		return new FeatureHandlerRepositoryFactory() {
			
			@Override
			public <E extends ClassifiedEvent> HandlerRepository<E> create() {
				
				Map<Integer, FeatureHandler<E>> featureHandlers = new ImmutableMap.Builder<Integer, FeatureHandler<E>>()
						
																						//.put(1, new SequentialDistributionFeatureHandler<E>()) // set age as a continuous feature
																						.put(11, new SequentialDistributionFeatureHandler<E>()) // set capital gains recorded
																						//.put(12, new SequentialDistributionFeatureHandler<E>()) // set capital loss recorded
																						//.put(13, new SequentialDistributionFeatureHandler<E>()) // set hours per week
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

	@SuppressWarnings("unchecked")
	@Override
	protected <E extends Message<E>> void addCSVRecordToEvents(CSVRecord record, Collection<E> events) {
		
		ContiuousTestEvent event = new ContiuousTestEvent();
		
		List<DynamicFeature<?>> features = new ArrayList<DynamicFeature<?>>();
		
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
	
	private DynamicFeature<?> getFeature(String featureValue, int type){
		
		featureValue = featureValue.trim();
		
		DynamicFeature<?> feature;
		
		//if (type == 1 || type == 11 || type == 12 || type == 13){
		if (type == 11){
			ContiuousTestEvent.IntegerFeature intFeature = new ContiuousTestEvent.IntegerFeature();
			intFeature.setValue(Integer.parseInt(featureValue));
			
			feature = intFeature;
		} else{
			ContiuousTestEvent.StringFeature stringFeature = new ContiuousTestEvent.StringFeature();
			stringFeature.setValue(featureValue);
			
			feature = stringFeature;
		}
		
		feature.setType(type);
		
		return feature;
	}
	
	public static class ContiuousTestEvent implements ClassifiedEvent, Message<ContiuousTestEvent>{
		
		final static Schema<ContiuousTestEvent> SCHEMA = createDynamicSchema();

		private List<? extends DynamicFeature<?>> featuresList; 
		private List<? extends com.haines.ml.rce.transport.Event.Classification> classificationsList; 
		
		@Override
		public List<? extends DynamicFeature<?>> getFeaturesList() {
			return featuresList;
		}

		private static Schema<ContiuousTestEvent> createDynamicSchema() {
			return RuntimeSchema.createFrom(ContiuousTestEvent.class);
		}

		@Override
		public List<? extends com.haines.ml.rce.transport.Event.Classification> getClassificationsList() {
			return classificationsList;
		}

		@Override
		public Schema<ContiuousTestEvent> cachedSchema() {
			return SCHEMA;
		}
		
		public void setClassificationsList(List<? extends com.haines.ml.rce.transport.Event.Classification> classificationsList) {
			this.classificationsList = classificationsList;
		}

		public void setFeaturesList(List<? extends DynamicFeature<?>> featuresList) {
			this.featuresList = featuresList;
		}
		
		@Override
		public int hashCode(){
			return 37 * featuresList.hashCode() + classificationsList.hashCode();
		}
		
		public boolean equals(Object obj){
			if (this == obj){
                return true;
            }
            if (obj == null || !(obj instanceof ContiuousTestEvent)){
                return false;
            }
            
            ContiuousTestEvent other = (ContiuousTestEvent)obj;
            
            return featuresList.size() == other.featuresList.size() && this.featuresList.containsAll(other.featuresList) && other.featuresList.containsAll(this.featuresList) &&
            		classificationsList.size() == other.classificationsList.size() && this.classificationsList.containsAll(other.classificationsList) && other.classificationsList.containsAll(this.classificationsList);
		}

		public static abstract class DynamicFeature<T extends DynamicFeature<T>> implements Message<T>, Feature{
			
			private int type;
			
			@Override
			public int getType() {
				return type;
			}
			
			public void setType(int type){
				this.type = type;
			}
			
			public abstract Object getValue();

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + type;
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj){
	                return true;
	            }
	            if (obj == null || !(obj instanceof DynamicFeature)){
	                return false;
	            }
	            @SuppressWarnings("unchecked")
	            DynamicFeature<T> other = (DynamicFeature<T>) obj;
	            

	            if (this.getValue() != null && other.getValue() != null) 
	            {
	                if (!this.getValue().equals(other.getValue())) {
	                    return false;
	                }
	            }
	            else if (this.getValue() == null ^ other.getValue() == null) {
	                return false;
	            }


	            if( type != other.type){
	            	return false;
	            }
	            
	            return true;
			}
		}
		
		
		public static class StringFeature extends DynamicFeature<StringFeature>{

			private final static Schema<StringFeature> SCHEMA = RuntimeSchema.createFrom(StringFeature.class);
			
			private String value;
			
			@Override
			public Schema<StringFeature> cachedSchema() {
				return SCHEMA;
			}

			@Override
			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}
		}
		
		public static class IntegerFeature extends DynamicFeature<IntegerFeature> {

			private static final Schema<IntegerFeature> SCHEMA = RuntimeSchema.createFrom(IntegerFeature.class);
			
			private Integer value;
			
			@Override
			public Schema<IntegerFeature> cachedSchema() {
				return SCHEMA;
			}

			@Override
			public Integer getValue() {
				return value;
			}

			public void setValue(Integer value) {
				this.value = value;
			}
			
		}
	}
}
