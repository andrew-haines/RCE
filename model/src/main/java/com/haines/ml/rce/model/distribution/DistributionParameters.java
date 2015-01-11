package com.haines.ml.rce.model.distribution;

import org.apache.commons.math3.util.FastMath;

public class DistributionParameters {
	
	public final static Math MATHS = new Math();

	private final int numSamples;
	private final double mean;
	private final double variance;
	private final double twoVariance;
	private final double oneOverSqrt2PiVariance;
	
	public DistributionParameters(int numSamples, double mean, double variance){
		this.numSamples = numSamples;
		this.mean = mean;
		this.variance = variance;
		this.twoVariance = 2 * variance;
		this.oneOverSqrt2PiVariance = 1 / (FastMath.sqrt(twoVariance * FastMath.PI)) ;
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
			int numSamples = dist1.numSamples + dist2.numSamples;
			
			double mean = ((dist1.numSamples * dist1.mean) + (dist2.numSamples * dist2.mean)) / numSamples;
			double variance = (dist1.numSamples * dist1.variance) + (dist2.numSamples * dist2.variance);
			
			variance = variance + dist1.numSamples * FastMath.pow(dist1.mean - mean, 2);
			variance = variance + dist2.numSamples * FastMath.pow(dist2.mean - mean, 2);
			variance = variance / numSamples;
			
			return new DistributionParameters(numSamples, mean, variance);
		}
		
		public DistributionParameters sub(DistributionParameters dist1, DistributionParameters dist2){
			int numSamples = dist1.numSamples - dist2.numSamples;
			
			double mean = ((dist1.numSamples * dist1.mean) - (dist2.numSamples * dist2.mean)) / numSamples;
			double variance = (dist1.numSamples * dist1.variance) - (dist2.numSamples * dist2.variance);
			
			variance = variance - dist1.numSamples * FastMath.pow(dist1.mean - mean, 2);
			variance = variance - dist2.numSamples * FastMath.pow(dist2.mean - mean, 2);
			variance = variance / numSamples;
			
			return new DistributionParameters(numSamples, mean, variance);
		}
	}
}
