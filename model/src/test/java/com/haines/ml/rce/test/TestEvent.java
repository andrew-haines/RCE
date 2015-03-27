package com.haines.ml.rce.test;

import java.util.List;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;

public class TestEvent implements ClassifiedEvent {

	private final List<Feature> features;
	private final List<Classification> classifications;
	
	@SuppressWarnings("unchecked")
	public TestEvent(List<? extends Feature> features, List<? extends Classification> classifications){
		this.features = (List<Feature>)features;
		this.classifications = (List<Classification>)classifications;
	}
	
	@Override
	public List<Feature> getFeaturesList() {
		return features;
	}

	@Override
	public List<Classification> getClassificationsList() {
		return classifications;
	}
	
	@Override
	public String toString(){
		return "TestEvent{ features="+Iterables.toString(features)+", classes="+Iterables.toString(classifications);
	}
}