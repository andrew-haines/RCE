package com.haines.ml.rce.test;

import com.haines.ml.rce.model.Feature;

public class TestFeature implements Feature {
	
	private final String featureStr;
	
	public TestFeature(String featureStr){
		this.featureStr = featureStr;
	}
	
	@Override
	public int hashCode() {
		return featureStr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return featureStr.equals(((TestFeature)obj).featureStr);
	}
	
	@Override
	public String toString(){
		return featureStr;
	}
}