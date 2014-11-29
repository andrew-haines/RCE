package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public interface NaiveBayesIndexes {

	public static final int NO_INDEX_FOUND = -1;
	
	Iterable<NaiveBayesPriorProperty> getPriors();
	
	Iterable<NaiveBayesPosteriorProperty> getPosteriors();

	int getPosteriorIndex(Feature feature, Classification classification);

	int getPriorIndex(Classification classification);

	int getMaxIndex();

	void clear();

	NaiveBayesIndexes copy();

	NaiveBayesIndexesProvider getGlobalIndexes();
}

