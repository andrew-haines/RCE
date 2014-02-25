package com.haines.ml.rce.aggregator;

import java.util.Map;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;

public class Aggregator {

	private final Map<Classification, Map<Feature, Integer>> posteriorCounts;
	private final Map<Classification, Integer> priorCounts;
	
	public Aggregator(Map<Classification, Map<Feature, Integer>> posteriorCounts, Map<Classification, Integer> priorCounts){
		this.posteriorCounts = posteriorCounts;
		this.priorCounts = priorCounts;
	}
	
	public void aggregate(){
		
	}
}
