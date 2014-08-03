package com.haines.ml.rce.test;

import java.util.Collection;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

public class TestEvent implements ClassifiedEvent{

	private final Collection<Feature> features;
	private final Collection<Classification> classification;
	
	public TestEvent(Collection<Feature> features, Collection<Classification> classification){
		this.features = features;
		this.classification = classification;
	}
	
	@Override
	public Collection<Feature> getFeatures() {
		return features;
	}

	@Override
	public Collection<Classification> getClassifications() {
		return classification;
	}

}
