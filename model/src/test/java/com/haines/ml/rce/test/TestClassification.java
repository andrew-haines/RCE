package com.haines.ml.rce.test;

import com.haines.ml.rce.model.Classification;

public class TestClassification implements Classification {
	
	private final String classificationStr;
	
	public TestClassification(String classificationStr){
		this.classificationStr = classificationStr;
	}
	
	@Override
	public int hashCode() {
		return classificationStr.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return classificationStr.equals(((TestClassification)obj).classificationStr);
	}
	
	@Override
	public String toString(){
		return classificationStr;
	}
}
