package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public interface NaiveBayesCountsProvider {

	Counts getCounts();
	
	public interface Counts{

		public Iterable<NaiveBayesCounts<NaiveBayesPriorProperty>> getPriors();

		public Iterable<NaiveBayesCounts<NaiveBayesPosteriorProperty>> getPosteriors();
	}
}
