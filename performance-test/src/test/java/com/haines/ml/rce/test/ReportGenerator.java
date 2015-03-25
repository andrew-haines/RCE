package com.haines.ml.rce.test;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.service.ClassifierService;
import com.haines.ml.rce.service.ClassifierService.PredicatedClassification;

public class ReportGenerator {

	private final int numOfTests;
	private final int numRocSteps;
	private final PerformanceTest test;
	private final String reportName;
	
	public ReportGenerator(String reportName, int numTests, int numRocSteps, PerformanceTest test){
		this.reportName = reportName;
		this.numOfTests = numTests;
		this.numRocSteps = numRocSteps;
		this.test = test;
	}
	
	private Report runReport(Collection<? extends ClassifiedEvent> trainingSet, Collection<? extends ClassifiedEvent> testSet, List<? extends Classification> classes){
		
		long heapSize = getMemoryAfterGC();
		
		for (ClassifiedEvent event: trainingSet){
			test.sendEvent(event);
		}
		
		test.notifyTrainingCompleted();
		
		long heapAfterTrainingSize = getMemoryAfterGC();
		
		ClassifierService classifierService = test.getClassifierService();
		
		int numEventsSeen = 0;
		int numEventsCorrectlyPredicted = 0;
		
		// set up 1 vs all
		
		int[] tp = new int[classes.size()];
		int[] fp = new int[classes.size()];
		int[] tn = new int[classes.size()];
		int[] fn = new int[classes.size()];
		int[] rocTp = new int[numRocSteps+1];
		int[] rocFp = new int[numRocSteps+1];
		int[] rocTn = new int[numRocSteps+1];
		int[] rocFn = new int[numRocSteps+1];
		
		for (ClassifiedEvent event: testSet){
			PredicatedClassification predictedClassification = classifierService.getClassification(event.getFeaturesList());
			
			if (event.getClassificationsList().contains(predictedClassification.getClassification())){
				numEventsCorrectlyPredicted++;
			}
			
			numEventsSeen++;
			
			double[] scores = getScores(classes, event, classifierService);
			
			double maxScore = StatUtils.max(scores);
			
			for (int i = 0; i < classes.size(); i++){
				
				Classification classification = classes.get(i);
				
				double scoreForClass = classifierService.getScore(event.getFeaturesList(), classification);
				double normalisedScore = scoreForClass / maxScore;
				
				for (int step = 0; step <= numRocSteps; step++){
					double threshold = step / (double)numRocSteps;
					
					if (normalisedScore > threshold){
						if (event.getClassificationsList().contains(predictedClassification.getClassification())){
							rocTp[step]++;
						} else{
							rocFp[step]++;
						}
					} else{
						
						if (event.getClassificationsList().contains(predictedClassification.getClassification())){
							rocTn[step]++;
						} else{
							rocFn[step]++;
						}
					}
				}
				
				if (event.getClassificationsList().contains(predictedClassification.getClassification())){
					if (predictedClassification.getClassification().getValue().equals(classification.getValue())){
						tp[i]++;
					} else{
						tn[i]++;
					}
				} else{
					if (predictedClassification.getClassification().getValue().equals(classification.getValue())){
						fp[i]++;
					} else{
						fn[i]++;
					}
				}
			}
		}
		
		double[][] rocData = getRocData(rocTp, rocFp, rocTn, rocFn);
		
		// now average our 1 vs all results
		
		double avTp = getAverage(tp);
		double avFp = getAverage(fp);
		double avTn = getAverage(tn);
		double avFn = getAverage(fn);
		
		double accuracy1 = (double)numEventsCorrectlyPredicted / (double)numEventsSeen;
		double accuracy2 = (avTp + avTn) / testSet.size();
		
		double precision = getPrecision(avTp, avFp);
		double recall = getRecall(avTp, avFp);
		
		double fmeasure = 2 * ((precision * recall) / precision + recall);
		
		assert(DoubleMath.fuzzyEquals(accuracy1, accuracy2, 0.0001)) : "accuracies do not equate: "+accuracy1+", "+accuracy2;
		
		return new Report((double)numEventsCorrectlyPredicted / (double)numEventsSeen, fmeasure, 0, FastMath.max(0, heapAfterTrainingSize - heapSize), 1, rocData, reportName);
	}

	private double[] getScores(List<? extends Classification> classes, ClassifiedEvent event, ClassifierService classifierService) {
		
		double[] scores = new double[classes.size()];
		for (int i = 0; i < classes.size(); i++){
			scores[i] = classifierService.getScore(event.getFeaturesList(), classes.get(i));
		}
		return scores;
	}

	private double[][] getRocData(int[] rocTp, int[] rocFp, int[] rocTn, int[] rocFn) {
		
		double[] tpr = new double[numRocSteps+1];
		double[] fpr = new double[numRocSteps+1];
		
		for (int step = 0; step <= numRocSteps; step++){
			tpr[step] = getRecall(rocTp[step], rocFn[step]);
			fpr[step] = getFallout(rocFp[step], rocTn[step]);
		}
		return new double[][]{fpr, tpr};
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
	
	private double getAverage(int[] values) {
		int sum = 0;
		
		for (int value: values){
			sum += value;
		}
		return sum / values.length;
	}
	
	private static double[][] getAverage(double[][] values1, double[][] values2){
		double[][] averagedValues = new double[values1.length][values1[0].length];
		
		for (int i = 0;i < values1.length; i++){
			
			for (int j = 0; j < values1[i].length; j++){
				averagedValues[i][j] = (values1[i][j] + values2[i][j]) / 2;
			}
		}
		
		return averagedValues;
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
			
			test.reset();
			
			@SuppressWarnings("unchecked")
			Report report = runReport((List<ClassifiedEvent>)trainingSet, (List<ClassifiedEvent>)testSet, classes);
			
			if (combinedReport == null){
				combinedReport = report;
			} else{
				combinedReport = combinedReport.avg(report);
			}
		}
		
		return combinedReport;
	}
	
	public Report getReport(Collection<? extends ClassifiedEvent> events, int numFolds, List<? extends Classification> classes){
		int foldSize = (int)Math.floor(events.size() / numFolds);
		
		Report combinedReport = null;
		
		for (int i = 0; i < numFolds; i++){
			
			Iterable<? extends ClassifiedEvent> trainingSet = getFold(i, foldSize, events);
			Iterable<? extends ClassifiedEvent> testSet = getInverseFold(i, foldSize, events);
			
			Report results = getReport(trainingSet, testSet, classes);
			
			if (combinedReport == null){
				combinedReport = results;
			} else{
				combinedReport = combinedReport.avg(results);
			}
		}
		
		return combinedReport;
	}
	
	private long getMemoryAfterGC() {
		
		Runtime rt = Runtime.getRuntime();
		
		rt.gc();
		
		long usedMemory = rt.totalMemory() - rt.freeMemory();
		
		return usedMemory;
	}
	
	private Iterable<? extends ClassifiedEvent> getInverseFold(int i, int foldSize, Collection<? extends ClassifiedEvent> events) {
		return Iterables.concat(Iterables.limit(events, i*foldSize), getFold(i+1, foldSize, events));
	}

	private Iterable<? extends ClassifiedEvent> getFold(int foldNum, int foldSize, Collection<? extends ClassifiedEvent> events) {
		
		return Iterables.limit(Iterables.skip(events, foldNum * foldSize), foldSize);
	}

	public static final class Report {
		
		private final double accuracy;
		private final double fmeasure;
		private final double roc;
		private final long numBytesUsedForModel;
		private final int numTestsRun;
		private final double[][] rocData;
		private final String reportName;
		
		private Report(double accuracy, double fmeasure, double roc, long numBytesUsedForModel, int numTestsRun, double[][] rocData, String reportName){
			this.accuracy = accuracy;
			this.fmeasure = fmeasure;
			this.roc = roc;
			this.numBytesUsedForModel = numBytesUsedForModel;
			this.numTestsRun = numTestsRun;
			this.rocData = rocData;
			this.reportName = reportName;
		}
		
		private Report avg(Report otherReport){
			return new Report((getAccuracy() + otherReport.getAccuracy()) / 2, 
					(getFmeasure() + otherReport.getFmeasure()) / 2, 
					(getRoc() + otherReport.getRoc()) / 2, 
					(long)FastMath.floor((getNumBytesUsedForModel() + otherReport.getNumBytesUsedForModel()) / 2), 
					getNumTestsRun() + otherReport.getNumTestsRun(),
					getAverage(this.rocData, otherReport.rocData), reportName);
		}

		public double getAccuracy() {
			return accuracy;
		}

		public double getFmeasure() {
			return fmeasure;
		}

		public double getRoc() {
			return roc;
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
	}
}
