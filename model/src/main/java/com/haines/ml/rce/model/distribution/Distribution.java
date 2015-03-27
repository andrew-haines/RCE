package com.haines.ml.rce.model.distribution;

import org.apache.commons.math3.util.FastMath;

public interface Distribution {
	
	public static Distribution NORMAL_DISTRIBUTION = new NormalDistribution();

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
