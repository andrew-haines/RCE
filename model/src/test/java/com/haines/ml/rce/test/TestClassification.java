package com.haines.ml.rce.test;

import com.haines.ml.rce.model.Classification;

public class TestClassification implements Classification {
	
	private static final int DEFAULT_CLASSIFICATION_TYPE = 0;
	private final String classificationStr;
	private final int classificationType;
	
	public TestClassification(String classificationStr, int classificationType){
		this.classificationStr = classificationStr;
		this.classificationType = classificationType;
	}
	
	public TestClassification(String classificationStr){
		this(classificationStr, DEFAULT_CLASSIFICATION_TYPE);
	}
	
	@Override
	public int hashCode() {
		return 31 * classificationStr.hashCode() + 31 * classificationType;
	}

	@Override
	public boolean equals(Object obj) {
		return classificationStr.equals(((TestClassification)obj).classificationStr) && classificationType == ((TestClassification)obj).classificationType;
	}
	
	@Override
	public String toString(){
		return classificationStr;
	}

	@Override
	public String getValue() {
		return classificationStr;
	}

	@Override
	public int getType() {
		return classificationType;
	}
}
