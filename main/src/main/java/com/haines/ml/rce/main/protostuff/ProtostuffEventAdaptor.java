package com.haines.ml.rce.main.protostuff;

import com.haines.ml.rce.model.ClassifiedEvent;

public abstract class ProtostuffEventAdaptor implements ClassifiedEvent{

	@Override
	public int hashCode(){
		int hash = 31;
		
		hash ^= getFeaturesList().hashCode();
		hash ^= getClassificationsList().hashCode();
		
		return hash;
	}
	
	@Override
	public boolean equals(Object obj){
		
		ProtostuffEventAdaptor other = (ProtostuffEventAdaptor)obj;
		
		return getFeaturesList().equals(other.getFeaturesList()) &&
				getClassificationsList().equals(other.getClassificationsList());
	}
	
	@Override
	public String toString(){
		return "{"+getFeaturesList()+"|"+getClassificationsList()+"}";
	}
}
