package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.distribution.Distribution;
import com.haines.ml.rce.model.distribution.DistributionParameters;

public interface DistributionProvider {

	/**
	 * Return parameters of a distribution stored within the accumulator at the slots defined.
	 * @param accumulator
	 * @param slots
	 * @return
	 */
	<E extends Event> DistributionParameters getDistribution(AccumulatorProvider<E> accumulator, int[] slots);
	
	/**
	 * Returns a distribution that this provider works with
	 * @return
	 */
	Distribution getDistribution();
}
