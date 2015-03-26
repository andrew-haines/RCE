package com.haines.ml.rce.test.model;

import java.util.List;

import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

public class ContinuousTestEvent implements ClassifiedEvent, Message<ContinuousTestEvent>{
	
	public final static Schema<ContinuousTestEvent> SCHEMA = createDynamicSchema();

	private List<? extends DynamicFeature<?>> featuresList; 
	private List<? extends com.haines.ml.rce.transport.Event.Classification> classificationsList; 
	
	@Override
	public List<? extends DynamicFeature<?>> getFeaturesList() {
		return featuresList;
	}

	private static Schema<ContinuousTestEvent> createDynamicSchema() {
		return RuntimeSchema.createFrom(ContinuousTestEvent.class);
	}

	@Override
	public List<? extends com.haines.ml.rce.transport.Event.Classification> getClassificationsList() {
		return classificationsList;
	}

	@Override
	public Schema<ContinuousTestEvent> cachedSchema() {
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
        if (obj == null || !(obj instanceof ContinuousTestEvent)){
            return false;
        }
        
        ContinuousTestEvent other = (ContinuousTestEvent)obj;
        
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
			int hash = 13;

    	    hash ^= this.getValue().hashCode();

    	    hash ^= this.type;

    	    
    	    return hash;
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
