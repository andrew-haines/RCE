package com.haines.ml.rce.naivebayes;

/**
 * This class acts as a gateway to a single instance of probabilites of naive bayes classification. With out this, threading issues
 * would need explicit locking. Using this class, a single immutable memory instance can be referenced and returned, keeping all
 * posterior and priors consistant. Without this class, locking would be required over all atomic
 * {@link NaiveBayesProbabilities#getPosteriorProbability(com.haines.ml.rce.model.Feature, com.haines.ml.rce.model.Classification)}
 * and {@link NaiveBayesProbabilities#getPriorProbability(com.haines.ml.rce.model.Classification)} transactional calls.
 * 
 * @author haines
 *
 */
public interface NaiveBayesProbabilitiesProvider {

	NaiveBayesProbabilities getProbabilities();
}
