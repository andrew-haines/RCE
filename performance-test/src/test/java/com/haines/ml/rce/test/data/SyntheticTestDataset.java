package com.haines.ml.rce.test.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.AbstractMultivariateRealDistribution;
import org.apache.commons.math3.distribution.MixtureMultivariateRealDistribution;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.util.Pair;

import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.model.DataSet;
import com.haines.ml.rce.transport.Event;
import com.haines.ml.rce.transport.Event.Classification;
import com.haines.ml.rce.transport.Event.Feature;
import com.haines.ml.rce.transport.ValueType;

public class SyntheticTestDataset implements DataSet{

	private final List<Classification> possibleClasses;
	private final RealDistribution classDistribution;
	private final int numFeatures;
	private final double probabilityOfFeatureBeingPresent;
	private final Map<Classification, MultivariateRealDistribution> underlyingFeatureClassDistributions;
	
	public SyntheticTestDataset(int numPossibleClasses, double probabilityOfFeatureBeingPresent, double[][] means, double[][][] covariance){
		
		assert(means.length == covariance.length);
		
		this.possibleClasses = new ArrayList<Classification>(numPossibleClasses);
		this.underlyingFeatureClassDistributions = new HashMap<Classification, MultivariateRealDistribution>();
		this.classDistribution = new WeibullDistribution(1, 1.5);
		
		for (int i = 0; i < numPossibleClasses; i++){
			
			Classification testClass = new Classification();
			
			testClass = new Classification();
			
			testClass.setValueType(ValueType.STRING);
			testClass.setStringValue("class_"+i);
			
			possibleClasses.add(testClass);
			
			underlyingFeatureClassDistributions.put(testClass, addNoise(new MultivariateNormalDistribution(means[i], covariance[i])));
		}
		
		this.numFeatures = means[0].length;
		this.probabilityOfFeatureBeingPresent = probabilityOfFeatureBeingPresent;
	}
	
	private MultivariateRealDistribution addNoise(MultivariateNormalDistribution normalDistribution) {
		return new MixtureMultivariateRealDistribution<AbstractMultivariateRealDistribution>(Arrays.asList(new Pair<Double, AbstractMultivariateRealDistribution>(0.7, normalDistribution), new Pair<Double, AbstractMultivariateRealDistribution>(0.3, new AbstractMultivariateRealDistribution(new JDKRandomGenerator(), normalDistribution.getDimension()){

			@Override
			public double density(double[] x) {
				return 0;
			}

			@Override
			public double[] sample() {
				double[] randoms = new double[getDimension()];
				
				for (int i = 0; i < randoms.length; i++){
					randoms[i] = this.random.nextDouble() * 100;
				}
				
				return randoms;
			}
			
		})));
	}

	public SyntheticTestDataset(int numPossibleClasses, int numFeatures, double probabilityOfFeatureBeingPresent){
		// provides a randomised constructor of mean values
		this(numPossibleClasses, probabilityOfFeatureBeingPresent, getRandomMeans(numFeatures, numPossibleClasses, 2), getRandomCovariances(numFeatures, numPossibleClasses));
	}
	
	private static double[][][] getRandomCovariances(int numFeatures, int numClasses) {
		
		/* 
		 * reuse this method to get random variances. Note that such a high variance upper limit makes the discrete classifier perform poorly 
		 * as there are an awful lot of possible 'bins' (integer numbers) that can be used.
		 */
		
		double[][] randomVariances = getRandomMeans(numFeatures, numClasses, 3);  
		double[][][] randomCovarianceMatrix = new double[numClasses][][];
		for (int i = 0; i < randomVariances.length; i++){
			randomCovarianceMatrix[i] = MatrixUtils.createRealDiagonalMatrix(randomVariances[i]).getData();
		}
		return randomCovarianceMatrix;
	}

	private static double[][] getRandomMeans(int numFeatures, int numClasses, double multiplier) {
		double[][] means = new double[numClasses][numFeatures];
		
		for (int i = 0; i < numClasses; i++){

			means[i] = getDeterministicRandomNumbers(numFeatures, (i+1 * multiplier));
		}
		
		return means;
	}
	
	private static double[] getDeterministicRandomNumbers(int numFeatures, double multiplier){
		double[] means = new double[numFeatures];
		
		if (multiplier < 1){
			multiplier = 1;
		}
		
		for (int i = 0; i < numFeatures; i++){
			int n = (int)(Math.pow(2, i) * ((2 % (i+1) == 0)?-1:1));
			means[i] = (((n << 1) ^ (n >> 31)) * multiplier) + multiplier; // use zipzag encoding to generate a deterministic distribution of positive means based on the number of features
		}
		
		return means;
	}

	@Override
	public List<? extends Classification> getExpectedClasses() {
		return possibleClasses;
	}
	
	/**
	 * Returns an iterable with the provided number of instances. If num of instances is -1 then this iterator
	 * will return an infinite stream of from the underlying distribution instances
	 * @param numInstances
	 * @return
	 */
	public Iterable<ClassifiedEvent> getEventsFromDistribution(final int numInstances){
		
		return new Iterable<ClassifiedEvent>(){

			private int currentEventNum = 0;
			@Override
			public Iterator<ClassifiedEvent> iterator() {
				return new Iterator<ClassifiedEvent>(){

					@Override
					public boolean hasNext() {
						return (currentEventNum < numInstances) || numInstances == -1;
					}

					@Override
					public Event next() {
						try{
							return generateNewTestEvent();
						} finally{
							currentEventNum++;
						}
					}

					@Override
					public void remove() {
						// NO OP
					}
					
				};
			}
			
		};
	}
	
	private Event generateNewTestEvent(){
		Classification expectedClass = getExpectedClass();
		
		List<Feature> features = new ArrayList<Feature>();
		
		double[] featureValuesFromDistribution = getFeatureValuesFromDistribution(expectedClass);
		
		for (int i = 0; i< numFeatures; i++){
			if (((i == numFeatures-1) && features.isEmpty()) || Math.random() <= probabilityOfFeatureBeingPresent){
				
				Feature feature = new Feature();
				
				feature.setValueType(ValueType.DOUBLE);
				feature.setDoubleValue(round(featureValuesFromDistribution[i], 0));
				feature.setType(i);
				
				features.add(feature); // round to integer
			}
		}
		
		Event event = new Event();
		
		event.setClassificationsList(Arrays.asList(expectedClass));
		event.setFeaturesList(features);
		
		return event;
	}

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    if (Double.isNaN(value)){
	    	value = 0.0;
	    };
	    
	    if (Double.isInfinite(value)){
	    	value = Double.MAX_VALUE;
	    }
	    
	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

	private double[] getFeatureValuesFromDistribution(Classification expectedClass) {
		
		return underlyingFeatureClassDistributions.get(expectedClass).sample();
	}

	private Classification getExpectedClass() {
		double randomClassSample1 = classDistribution.sample();
		double randomClassSample2 = classDistribution.sample();
		
		
		// returns a sample between 0-1
		double normalisedSample = (randomClassSample1 > randomClassSample2)? randomClassSample2 / randomClassSample1: randomClassSample1 / randomClassSample2;
		
		int classIdx = (int)(normalisedSample * possibleClasses.size());
		
		return possibleClasses.get(classIdx);
	}
}
