package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.Probability;

public class NaiveBayesProbability{

	private final NaiveBayesProperty property;
	private final Probability probability;
	
	NaiveBayesProbability(NaiveBayesProperty property, Probability probability){
		this.property = property;
		this.probability = probability;
	}
	
	public NaiveBayesProperty getProperty() {
		return property;
	}
	
	public Probability getProbability() {
		return probability;
	}
	
	@Override
	public String toString(){
		return property+"="+probability;
	}
}
