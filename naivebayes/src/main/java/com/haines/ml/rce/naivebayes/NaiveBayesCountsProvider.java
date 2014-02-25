package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public interface NaiveBayesCountsProvider {

	Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriorCounts();
	
	Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriorCounts();
}
