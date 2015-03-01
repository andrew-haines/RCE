package com.haines.ml.rce.test;

import com.haines.ml.rce.model.Classification;

public class TestClassification implements Classification {
	
	private static final int DEFAULT_CLASSIFICATION_TYPE = 0;
	private final Object classification;
	private final int classificationType;
	
	public TestClassification(Object classificationStr, int classificationType){
		this.classification = classificationStr;
		this.classificationType = classificationType;
	}
	
	public TestClassification(Object classificationStr){
		this(classificationStr, DEFAULT_CLASSIFICATION_TYPE);
	}
	
	@Override
	public int hashCode() {
		return 31 * classification.hashCode() + 31 * classificationType;
	}

	@Override
	public boolean equals(Object obj) {
		return classification.equals(((TestClassification)obj).classification) && classificationType == ((TestClassification)obj).classificationType;
	}
	
	@Override
	public String toString(){
		return classification.toString();
	}

	@Override
	public Object getValue() {
		return classification;
	}

	@Override
	public int getType() {
		return classificationType;
	}
}
