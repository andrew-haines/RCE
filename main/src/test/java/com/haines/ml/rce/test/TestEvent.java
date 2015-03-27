package com.haines.ml.rce.test;

import java.util.Collection;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

public class TestEvent implements ClassifiedEvent{

	private final Collection<Feature> features;
	private final Collection<Classification> classifications;
	
	public TestEvent(Collection<Feature> features, Collection<Classification> classifications){
		this.features = features;
		this.classifications = classifications;
	}
	
	@Override
	public Collection<Feature> getFeaturesList() {
		return features;
	}

	@Override
	public Collection<Classification> getClassificationsList() {
		return classifications;
	}

	@Override
	public String toString(){
		return "TestEvent{ features="+Iterables.toString(features)+", classes="+Iterables.toString(classifications);
	}
}
