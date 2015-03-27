package com.haines.ml.rce.model.distribution;

import org.apache.commons.math3.util.FastMath;

public class DistributionParameters {
	
	public final static Math MATHS = new Math();

	public static final DistributionParameters EMPTY_DISTRIBUTION_PARAMETERS = new DistributionParameters();

	private final int numSamples;
	private final double mean;
	private final double variance;
	private final double twoVariance;
	private final double oneOverSqrt2PiVariance;
	
	public DistributionParameters(int numSamples, double mean, double variance){
		this.numSamples = numSamples;
		this.mean = mean;
		
		assert(numSamples > 0);
		assert(!Double.isNaN(variance) || numSamples == 1) : "variance="+variance+", numSamples="+numSamples+", mean="+mean; // only have NaN if we have 1 sample as this is the only case that the variance is undefined for this use case 
		
		this.variance = variance;
		this.twoVariance = 2 * variance;
		this.oneOverSqrt2PiVariance = 1 / (FastMath.sqrt(twoVariance * FastMath.PI)) ;
	}
	
	private DistributionParameters(){
		numSamples = 0;
		mean = 0;
		variance = 0;
		twoVariance = 0;
		oneOverSqrt2PiVariance = 0;
	}

	public int getNumSamples() {
		return numSamples;
	}

	public double getMean() {
		return mean;
	}

	public double getVariance() {
		return variance;
	}
	
	public double getTwoVarianceSquared() {
		return twoVariance;
	}
	
	public double get1OverSqrt2PiVarance() {
		return oneOverSqrt2PiVariance;
	}
	
	public final static class Math {
		
		/**
		 * Takes to distributions and returns a distribution that represents the addition of both sets of data that the input distributions
		 * describe. {@linkplain http://www.emathzone.com/tutorials/basic-statistics/combined-variance.html}
		 * @param dist1
		 * @param dist2
		 * @return
		 */
		public DistributionParameters add(DistributionParameters dist1, DistributionParameters dist2){
			
			if (dist1 == EMPTY_DISTRIBUTION_PARAMETERS){
				return dist2;
			}
			
			if (dist2 == EMPTY_DISTRIBUTION_PARAMETERS){
				return dist1;
			}
			
			int numSamples = dist1.numSamples + dist2.numSamples;
			
			double mean = ((dist1.numSamples * dist1.mean) + (dist2.numSamples * dist2.mean)) / numSamples;
			
			double var1 = Double.isNaN(dist1.variance)?0:dist1.variance;
			double var2 = Double.isNaN(dist2.variance)?0:dist2.variance;
			
			double variance = ((dist1.numSamples - 1) * var1) + ((dist2.numSamples - 1) * var2);
			
			variance = variance + dist1.numSamples * FastMath.pow(dist1.mean - mean, 2);
			variance = variance + dist2.numSamples * FastMath.pow(dist2.mean - mean, 2);
			variance = variance / (numSamples - 1); // note that we correct for the bias in variance here.
			
			return new DistributionParameters(numSamples, mean, variance);
		}
		
		public DistributionParameters sub(DistributionParameters dist1, DistributionParameters dist2){
			
			int numSamples = dist1.numSamples - dist2.numSamples;
			
			double mean = ((dist1.mean * dist1.numSamples) - (dist2.numSamples * dist2.mean)) / numSamples;
			
			double var1 = Double.isNaN(dist1.variance)?0:dist1.variance;
			double var2 = Double.isNaN(dist2.variance)?0:dist2.variance;
			
			double variance = ((var1 * (dist1.numSamples - 1)) - (var2 * (dist2.numSamples - 1)));
			variance = variance - dist2.numSamples * (FastMath.pow(dist2.mean - dist1.mean, 2));
			variance = variance - numSamples * (FastMath.pow(mean - dist1.mean, 2));
			variance = variance / (numSamples - 1);
			
			return new DistributionParameters(numSamples, mean, variance);
		}
	}
}
