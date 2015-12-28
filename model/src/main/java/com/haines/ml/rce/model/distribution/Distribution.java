package com.haines.ml.rce.model.distribution;

import org.apache.commons.math3.util.FastMath;

/**
 * An interface that defines a given distribution.
 * @author haines
 *
 */
public interface Distribution {
	
	/**
	 * A distribution corresponding to a normal or guassian distribution.
	 */
	public static final Distribution NORMAL_DISTRIBUTION = new NormalDistribution();

	/**
	 * Given some distribution parameters and a given input, returns a result that corresponds to the underlying distribution mechanics.
	 * @param params
	 * @param input
	 * @return
	 */
	double getValue(DistributionParameters params, double input);
	
	public static class NormalDistribution implements Distribution{

		private NormalDistribution(){}

		@Override
		public double getValue(DistributionParameters params, double input) {
			
			if (params.getVariance() == 0){
				// we have no variance so just return 1 if the input is the same at the mean
				
				if (input == params.getMean()){
					return 1;
				} else{
					return 0;
				}
			}
			
			return (params.get1OverSqrt2PiVarance()) * FastMath.exp(-(FastMath.pow(input - params.getMean(), 2) / params.getTwoVarianceSquared()));
		}
	}
}
