package com.haines.ml.rce.test;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sourceforge.sizeof.SizeOf;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.util.FastMath;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.service.ClassifierService;
import com.haines.ml.rce.service.ClassifierService.PredictedClassification;

public class ReportGenerator {

	private final int numOfTests;
	private final PerformanceTest test;
	private final String reportName;
	private static final UnivariateInterpolator INTERPOLATOR = new SplineInterpolator();
	private static final int NUM_ROC_STEPS = 1000;
	
	public ReportGenerator(String reportName, int numTests, PerformanceTest test){
		this.reportName = reportName;
		this.numOfTests = numTests;
		this.test = test;
	}
	
	private Report runReport(Collection<? extends ClassifiedEvent> trainingSet, Collection<? extends ClassifiedEvent> testSet, final List<? extends Classification> classes, RCEApplication<?> application){
		
		
		long startTime = System.currentTimeMillis();
		for (ClassifiedEvent event: trainingSet){
			
			//System.out.println(event.toString());
			test.sendEvent(event);
		}
		
		long timeToTrain = System.currentTimeMillis() - startTime;
		
		test.notifyTrainingCompleted();
		
		final ClassifierService classifierService = test.getClassifierService();
		
		startTime = System.currentTimeMillis();
		
		final Map<Classification, Integer> numPositives = new HashMap<Classification, Integer>();
		
		List<Scores> scores = Lists.newArrayList(Iterables.transform(testSet, new Function<ClassifiedEvent, Scores>(){

			@Override
			public Scores apply(ClassifiedEvent input) {
				
				Scores scores = new Scores(classifierService.getClassification(input.getFeaturesList()), input.getClassificationsList());
				
				for (Classification classification: classes){
					scores.addScore(classification, classifierService.getScore(input.getFeaturesList(), classification));
					
					if (input.getClassificationsList().contains(classification)){
						Integer currentCount = numPositives.get(classification);
						
						if (currentCount == null){
							currentCount = 0;
						}
						
						numPositives.put(classification, ++currentCount);
					}
				}
				
				return scores;
			}
		}));
		
		long timeToTest = System.currentTimeMillis() - startTime;
		

		Map<Classification, Report> reports = new HashMap<Classification, Report>();
		
		// now perform 1 vs all
		for (Classification positiveClassification: classes){
			
			Integer numPositiveInstances = numPositives.get(positiveClassification);
			
			if (numPositiveInstances == null){
				numPositiveInstances = 1;
			}
			
			reports.put(positiveClassification, getReport(scores, positiveClassification, numPositiveInstances));
		}
		
		Report avReport = null;
		
		double aucTotal = 0;
		
		for (Entry<Classification, Report> report: reports.entrySet()){
			if (avReport == null){
				avReport = report.getValue();
			} else{
				avReport = avReport.avg(report.getValue());
			}
			
			/*
			 * From: http://home.comcast.net/~tom.fawcett/public_html/papers/ROC101.pdf
			 * The disadvantage is that the class reference ROC is sensitive to class distributions and error costs, 
			 * so this formulation of AUCtotal is as well.
			 */
			
			Integer numPositiveInstances = numPositives.get(report.getKey());
			
			if (numPositiveInstances == null){
				numPositiveInstances = 1;
			}
			
			aucTotal += report.getValue().getAuc() * (numPositiveInstances / (double)scores.size()); 
		}
		
		return new Report(avReport.getAccuracy(), avReport.getFmeasure(), aucTotal, SizeOf.deepSizeOf(application), 1, avReport.getRocData(), reportName, timeToTrain, timeToTest);
	}

	private Report getReport(List<Scores> scores, Classification positiveClassification, int totalP) {
		Collections.sort(scores, new Scores.ScoreComparator(positiveClassification));

		int total = scores.size();
		
		int totalN = total - totalP;
		
		int fp = 0;
		int tp = 0;
		int tn = totalN;
		int fn = totalP;
		
		int correctlyPredicted = 0;
		
		double prevScore = Double.NEGATIVE_INFINITY;
		Deque<Double[]> stack = new ArrayDeque<Double[]>();
		
		int predictedTrue = 0;
		//stack.add(new Double[]{0.0, 0.0});
		
		// the following is an adaption of the algorithm in https://ccrma.stanford.edu/workshops/mir2009/references/ROCintro.pdf
		for (Scores score: scores){
			double positiveClassificationScore = score.getScore(positiveClassification);
			
			if (positiveClassificationScore != prevScore){
				stack.add(new Double[]{getFallout(fp, tn), getRecall(tp, fn)});
				prevScore = positiveClassificationScore;
			}
			
			if (score.getExpectedClassifications().contains(positiveClassification)){
				tp++;
				fn--;
			} else{
				fp++;
				tn--;
			}
			
			// ignoring what the positive classification is, is this classification's prediction the same as the actual classification. Used to calculate the accuracy
			if (score.getExpectedClassifications().contains(score.getPredictedClassification().getClassification())){
				correctlyPredicted++;
			}
		}
		stack.add(new Double[]{getFallout(fp, tn), getRecall(tp, fn)});
		
		double[][] rocData = new double[2][stack.size()];
		
		int i = 0;
		
		double yAxisSum = 0;
		double yAxisNumSameVals = 0;
		
		for (Double[] points: stack){
			
			// average points that have the same x axis values. This is required for the interpolation step when averaging ROC curves
			
			if (i == 0 || rocData[0][i-1] != points[0]){ // we now have a different x axis value so replace the previous y axis value 
				
				if (yAxisNumSameVals > 0){
					rocData[1][i-1] = (rocData[1][i-1] + yAxisSum) / (1+yAxisNumSameVals);
				}
				
				assert(points[0] >= 0 && points[0] <= 1);
				assert(points[1] >= 0 && points[1] <= 1);
				
				rocData[0][i] = points[0];
				rocData[1][i] = points[1];
				yAxisSum = 0;
				yAxisNumSameVals = 0;
				
				i++; // advance to next point
			} else{
				yAxisNumSameVals++;
				yAxisSum += points[1];
			}
		}
		
		double[][] rocDataTrimed = new double[2][i];
		
		System.arraycopy(rocData[0], 0, rocDataTrimed[0], 0, i);
		System.arraycopy(rocData[1], 0, rocDataTrimed[1], 0, i);
		
		double auc = getAuc(rocDataTrimed);
		
		double precision = getPrecision(tp, fp);
		double recall = getRecall(tp, fn);
		
		double fmeasure = 2 / ((1 / precision) + (1 / recall));
		
		double accuracy = correctlyPredicted / (double)total;
		
		return new Report(accuracy, fmeasure, auc, -1, 1, rocDataTrimed, reportName+"_"+positiveClassification.toString(), -1, -1);
	}

	private double getAuc(double[][] rocData) {
		
		double sum = 0;
		
		double prevFpr = 1;
		double prevTpr = 1;
		
		double[] fpr = rocData[0];
		double[] tpr = rocData[1];
		
		for (int i = fpr.length - 1; i >= 0; i--){
			assert(prevFpr >= fpr[i]); // check these are in the order we expect
			assert(prevTpr >= tpr[i]);
			
			sum += getAreaOfTrapezoid(prevFpr, fpr[i], prevTpr, tpr[i]);
			
			prevFpr = fpr[i];
			prevTpr = tpr[i];
		}
		
		return sum;
	}

	private final double getPrecision(double tp, double fp){
		return tp / (tp + fp);
	}
	
	private final double getRecall(double tp, double fn){ // also TPR
		return tp / (tp + fn);
	}
	
	private final double getFallout(double fp, double tn){ // also FPR
		return fp / (fp + tn);
	}
	
	private static double[][] getAverage(double[][] values1, double[][] values2){
		
		/*
		 *  To average these 2 arrays we need to interpolate their points to a set number of steps so that
		 *  are uniformly averaged.
		 */
		
		boolean directlyComparable = true;
		double[][] averagedPoints = new double[2][values1[1].length];
		
		if ((directlyComparable = values1[1].length == values2[1].length)){
			
			for (int i = 0; i < values1[1].length; i++){
				if (values1[1][i] == values2[1][i]){
					averagedPoints[0][i] = (values1[0][i] + values2[0][i]) / 2;
				} else{
					directlyComparable = false;
					break;
				}
			}
			
			if (directlyComparable){
				averagedPoints[1] = values1[1]; // y values are all the same so set directly
			}
		}
		
		if (!directlyComparable) {
		
			UnivariateFunction interpolatedPoints1 = INTERPOLATOR.interpolate(values1[0], values1[1]);
			UnivariateFunction interpolatedPoints2 = INTERPOLATOR.interpolate(values2[0], values2[1]);
			
			averagedPoints = new double[2][NUM_ROC_STEPS];
			
			double numSteps = (double)NUM_ROC_STEPS;
			
			for (int i = 0; i < NUM_ROC_STEPS; i++){
				
				double yValue = (double)i / numSteps;
				
				double value1 = interpolatedPoints1.value(yValue);
				double value2 = interpolatedPoints2.value(yValue);
				
				averagedPoints[0][i] = yValue;
				averagedPoints[1][i] = Math.max(Math.min((value1 + value2) / 2, 1), 0); // the spline interpolator can sometimes curve over 1 so cap here appropriately
				
				assert(averagedPoints[1][i] >= 0 && averagedPoints[1][i] <= 1): "averaged point "+averagedPoints[1][i]+" is not between 0-1";
			}
		}
		
		return averagedPoints;
	}

	public Report getReport(Iterable<? extends ClassifiedEvent> trainingSet, Iterable<? extends ClassifiedEvent> testSet, List<? extends Classification> classes){
		
		Report combinedReport = null;
		
		// first convert iterable into memory incase these are lazy loaded. Do this so our memory inspection is most accurate
		
		if (!(trainingSet instanceof List)){
			trainingSet = Lists.newArrayList(trainingSet);
		}
		
		if (!(testSet instanceof List)){
			testSet = Lists.newArrayList(testSet);
		}
		
		for (int i = 0; i < numOfTests; i++){
			
			RCEApplication<?> newApplication = test.reset();
			
			@SuppressWarnings("unchecked")
			Report report = runReport((List<ClassifiedEvent>)trainingSet, (List<ClassifiedEvent>)testSet, classes, newApplication);
			
			if (combinedReport == null){
				combinedReport = report;
			} else{
				combinedReport = combinedReport.avg(report);
			}
		}
		
		return combinedReport;
	}
	
	/**
	 * This method defines the training and test sets based on an n-fold validation strategy. 
	 * @param events
	 * @param numFolds
	 * @param classes
	 * @return
	 */
	public Report getReport(Collection<? extends ClassifiedEvent> events, int numFolds, List<? extends Classification> classes){
		
		List<ClassifiedEvent> shuffledEvents = Lists.newArrayList(events);
		
		Collections.shuffle(shuffledEvents);
		
		int foldSize = (int)Math.floor(shuffledEvents.size() / numFolds);
		
		Report combinedReport = null;
		
		for (int i = 0; i < numFolds; i++){
			
			Iterable<? extends ClassifiedEvent> trainingSet = getInverseFold(i, foldSize, shuffledEvents);
			Iterable<? extends ClassifiedEvent> testSet = getFold(i, foldSize, shuffledEvents);
			
			Report results = getReport(trainingSet, testSet, classes);
			
			if (combinedReport == null){
				combinedReport = results;
			} else{
				combinedReport = combinedReport.avg(results);
			}
		}
		
		return combinedReport;
	}
	
	/**
	 * foldSize = 3
	 * 
	 * data:
	 * [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
	 * 
	 * foldNum:
	 * |<- 1 ->||<- 2 ->||<- 3 ->||<-  4  ->||<-  5   ->||<-  6   ->|
	 * 
	 * returns:
	 * 
	 * [3,4,5,6,7,8,9,10,11,12,13,14,15,16,17]   [0,1,2,6,7,8,9,10,11,12,13,14,15,16,17]  [0,1,2,3,4,5,9,10,11,12,13,14,15,16,17]  [0,1,2,3,4,5,6,7,8,12,13,14,15,16,17]  [0,1,2,3,4,5,6,7,8,9,10,11,15,16,17]  [0,1,2,3,4,5,6,7,8,9,10,11,12,13,14]
	 * 
	 * @param foldNum
	 * @param foldSize
	 * @param events
	 * @return
	 */
	private Iterable<? extends ClassifiedEvent> getInverseFold(int i, int foldSize, Collection<? extends ClassifiedEvent> events) {
		return Iterables.concat(Iterables.limit(events, i*foldSize), Iterables.skip(events, (i*foldSize +foldSize)));
	}

	/**
	 * foldSize = 3
	 * 
	 * data:
	 * [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
	 * 
	 * foldNum:
	 * |<- 1 ->||<- 2 ->||<- 3 ->||<-  4  ->||<-  5   ->||<-  6   ->|
	 * 
	 * returns:
	 * 
	 * [0,1,2]   [3,4,5]  [6,7,8]  [9,10,11]  [12,13,14]  [15,16,17]
	 * 
	 * @param foldNum
	 * @param foldSize
	 * @param events
	 * @return
	 */
	private Iterable<? extends ClassifiedEvent> getFold(int foldNum, int foldSize, Collection<? extends ClassifiedEvent> events) {
		
		return Iterables.limit(Iterables.skip(events, foldNum * foldSize), foldSize);
	}

	public static final class Report {
		
		private final double accuracy;
		private final double fmeasure;
		private final double auc;
		private final long numBytesUsedForModel;
		private final int numTestsRun;
		private final double[][] rocData;
		private final String reportName;
		private final long timeToTrain, timeToTest;
		
		private Report(double accuracy, double fmeasure, double auc, long numBytesUsedForModel, int numTestsRun, double[][] rocData, String reportName, long timeToTrain, long timeToTest){
			this.accuracy = accuracy;
			this.fmeasure = fmeasure;
			this.auc = auc;
			this.numBytesUsedForModel = numBytesUsedForModel;
			this.numTestsRun = numTestsRun;
			this.rocData = rocData;
			this.reportName = reportName;
			this.timeToTrain = timeToTrain;
			this.timeToTest = timeToTest;
		}
		
		private Report avg(Report otherReport){
			return new Report((getAccuracy() + otherReport.getAccuracy()) / 2, 
					(getFmeasure() + otherReport.getFmeasure()) / 2, 
					(getAuc() + otherReport.getAuc()) / 2, 
					(long)FastMath.floor((getNumBytesUsedForModel() + otherReport.getNumBytesUsedForModel()) / 2), 
					getNumTestsRun() + otherReport.getNumTestsRun(),
					getAverage(this.rocData, otherReport.rocData), reportName,
					(this.timeToTrain + otherReport.timeToTrain) / 2,
					(this.timeToTest + otherReport.timeToTest) / 2);
		}

		public double getAccuracy() {
			return accuracy;
		}

		public double getFmeasure() {
			return fmeasure;
		}

		public double getAuc() {
			return auc;
		}

		public long getNumBytesUsedForModel() {
			return numBytesUsedForModel;
		}

		public int getNumTestsRun() {
			return numTestsRun;
		}
		
		public double[][] getRocData(){
			return rocData;
		}

		public String getReportName() {
			return reportName;
		}
		
		public long getTimeToTrain(){
			return timeToTrain;
		}
		
		public long getTimeToTest(){
			return timeToTest;
		}
	}
	
	private double getAreaOfTrapezoid(double x1, double x2, double y1, double y2){
		double area = FastMath.abs(x1 - x2);
		
		double avHeight = (y1 + y2) / 2;
		
		return area * avHeight;
	}
	
	private static class Scores {
		
		private static final class ScoreComparator implements Comparator<Scores>{

			private final Classification classToSortScoresBy;
			
			private ScoreComparator(Classification classToSortScoresBy){
				this.classToSortScoresBy = classToSortScoresBy;
			}
			
			@Override
			public int compare(Scores o1, Scores o2) {
				return o2.getScore(classToSortScoresBy).compareTo(o1.getScore(classToSortScoresBy)); // decreasing order of scores
			}
			
		}
		
		private final Map<Classification, Double> classificationScores = new HashMap<Classification, Double>();
		private final PredictedClassification predictedClassification;
		private final Collection<? extends Classification> expectedClassifications;
		private double maxScore = 0;
		
		private Scores(PredictedClassification classification, Collection<? extends Classification> expectedClassifications){
			this.predictedClassification = classification;
			this.expectedClassifications = expectedClassifications;
		}
		
		@SuppressWarnings("unchecked")
		public Collection<Classification> getExpectedClassifications() {
			return (Collection<Classification>)expectedClassifications;
		}
		
		public PredictedClassification getPredictedClassification(){
			return predictedClassification;
		}

		private void addScore(Classification classification, double score){
			classificationScores.put(classification, score);
			
			if (score > maxScore){
				maxScore = score;
			}
		}
		
		private Double getScore(Classification classification){
			return new Double(classificationScores.get(classification).doubleValue() / maxScore); // normalise
		}
	}
}
