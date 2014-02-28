package com.haines.ml.rce.test;

import java.util.Collection;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

public class TestEvent implements ClassifiedEvent {

	private final Collection<Feature> features;
	private final Collection<Classification> classifications;
	
	@SuppressWarnings("unchecked")
	public TestEvent(Collection<? extends Feature> features, Collection<? extends Classification> classifications){
		this.features = (Collection<Feature>)features;
		this.classifications = (Collection<Classification>)classifications;
	}
	
	@Override
	public Collection<Feature> getFeatures() {
		return features;
	}

	@Override
	public Collection<Classification> getClassifications() {
		return classifications;
	}
}