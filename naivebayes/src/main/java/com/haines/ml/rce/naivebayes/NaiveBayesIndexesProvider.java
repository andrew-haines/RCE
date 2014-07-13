package com.haines.ml.rce.naivebayes;

public interface NaiveBayesIndexesProvider {

	NaiveBayesIndexes getIndexes();
	
	void setIndexes(NaiveBayesIndexes indexes);

}
