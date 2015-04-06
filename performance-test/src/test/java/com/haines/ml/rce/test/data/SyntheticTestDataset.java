package com.haines.ml.rce.test.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.MultivariateRealDistribution;
import org.apache.commons.math3.distribution.RealDistribution;
import org.apache.commons.math3.distribution.WeibullDistribution;
import org.apache.commons.math3.linear.MatrixUtils;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.test.TestClassification;
import com.haines.ml.rce.test.TestEvent;
import com.haines.ml.rce.test.TestFeature;
import com.haines.ml.rce.test.model.DataSet;

public class SyntheticTestDataset implements DataSet{

	private final List<TestClassification> possibleClasses;
	private final RealDistribution classDistribution;
	private final int numFeatures;
	private final double probabilityOfFeatureBeingPresent;
	private final Map<TestClassification, MultivariateRealDistribution> underlyingFeatureClassDistributions;
	
	public SyntheticTestDataset(int numPossibleClasses, double probabilityOfFeatureBeingPresent, double[][] means, double[][][] covariance){
		
		assert(means.length == covariance.length);
		
		this.possibleClasses = new ArrayList<TestClassification>(numPossibleClasses);
		this.underlyingFeatureClassDistributions = new HashMap<TestClassification, MultivariateRealDistribution>();
		this.classDistribution = new WeibullDistribution(1, 1.5);
		
		for (int i = 0; i < numPossibleClasses; i++){
			
			TestClassification testClass = new TestClassification("class_"+i);
			possibleClasses.add(testClass);
			
			underlyingFeatureClassDistributions.put(testClass, new MultivariateNormalDistribution(means[i], covariance[i]));
			
		}
		this.numFeatures = means[0].length;
		this.probabilityOfFeatureBeingPresent = probabilityOfFeatureBeingPresent;
	}
	
	public SyntheticTestDataset(int numPossibleClasses, int numFeatures, double probabilityOfFeatureBeingPresent){
		// provides a randomised construtor of mean values
		this(numPossibleClasses, probabilityOfFeatureBeingPresent, getRandomMeans(numFeatures, numPossibleClasses, 1000), getRandomCovariances(numFeatures, numPossibleClasses));
	}
	
	private static double[][][] getRandomCovariances(int numFeatures, int numClasses) {
		double[][] randomVariances = getRandomMeans(numFeatures, numClasses, 10000); // reuse this method to get random variances
		double[][][] randomCovarianceMatrix = new double[numClasses][][];
		for (int i = 0; i < randomVariances.length; i++){
			randomCovarianceMatrix[i] = MatrixUtils.createRealDiagonalMatrix(randomVariances[i]).getData();
		}
		return randomCovarianceMatrix;
	}

	private static double[][] getRandomMeans(int numFeatures, int numClasses, int maxValue) {
		double[][] means = new double[numClasses][numFeatures];
		
		for (int i = 0; i < numClasses; i++){
			for (int j = 0; j < numFeatures; j++){
				means[i][j] = Math.random() * maxValue;
			}
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
					public TestEvent next() {
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
	
	private TestEvent generateNewTestEvent(){
		Classification expectedClass = getExpectedClass();
		
		Collection<Feature> features = new ArrayList<Feature>();
		
		double[] featureValuesFromDistribution = getFeatureValuesFromDistribution(expectedClass);
		
		for (int i = 0; i< numFeatures; i++){
			if (((i == numFeatures-1) && features.isEmpty()) || Math.random() <= probabilityOfFeatureBeingPresent){
				features.add(new TestFeature(round(featureValuesFromDistribution[i], 2), i)); // round to integer
			}
		}
		
		TestEvent event = new TestEvent(features, Arrays.asList(expectedClass));
		
		return event;
	}

	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

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
