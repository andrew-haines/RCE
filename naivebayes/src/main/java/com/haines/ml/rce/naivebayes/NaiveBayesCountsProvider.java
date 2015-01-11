package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;

public interface NaiveBayesCountsProvider {

	Counts getCounts();
	
	public interface Counts{

		public Iterable<NaiveBayesCounts<?>> getPriors();

		public Iterable<NaiveBayesCounts<?>> getPosteriors();
	}
}
