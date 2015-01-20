package com.haines.ml.rce.test;

import com.haines.ml.rce.model.Feature;

public class TestFeature implements Feature {
	
	private static final int DEFAULT_TYPE = 0;
	private final String featureStr;
	private final int type;
	
	public TestFeature(String featureStr, int type){
		this.featureStr = featureStr;
		this.type = type;
	}
	
	public TestFeature(String featureStr){
		this(featureStr, DEFAULT_TYPE);
	}
	
	@Override
	public int hashCode() {
		return 31 * featureStr.hashCode() + 31 * type;
	}

	@Override
	public boolean equals(Object obj) {
		return featureStr.equals(((TestFeature)obj).featureStr) && type == ((TestFeature)obj).type;
	}
	
	@Override
	public String toString(){
		return type+":"+featureStr;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public Object getValue() {
		return featureStr;
	}
}