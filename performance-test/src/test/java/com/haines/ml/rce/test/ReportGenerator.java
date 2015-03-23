package com.haines.ml.rce.test;

import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.service.ClassifierService;
import com.haines.ml.rce.service.ClassifierService.PredicatedClassification;

public class ReportGenerator {

	private final int numOfTests;
	private final PerformanceTest test;
	
	public ReportGenerator(int numTests, PerformanceTest test){
		this.numOfTests = numTests;
		this.test = test;
	}
	
	private Report runReport(Collection<? extends ClassifiedEvent> trainingSet, Collection<? extends ClassifiedEvent> testSet){
		
		long heapSize = getMemoryAfterGC();
		
		for (ClassifiedEvent event: trainingSet){
			test.sendEvent(event);
		}
		
		test.notifyTrainingCompleted();
		
		long heapAfterTrainingSize = getMemoryAfterGC();
		
		ClassifierService classifierService = test.getClassifierService();
		
		int numEventsSeen = 0;
		int numEventsCorrectlyPredicted = 0;
		
		for (ClassifiedEvent event: testSet){
			PredicatedClassification predictedClassification = classifierService.getClassification(event.getFeaturesList());
			
			if (event.getClassificationsList().contains(predictedClassification.getClassification())){
				numEventsCorrectlyPredicted++;
			}
			
			numEventsSeen++;
		}
		
		return new Report((double)numEventsCorrectlyPredicted / (double)numEventsSeen, 0, 0, heapAfterTrainingSize - heapSize, 1);
	}
	
	public Report getReport(Iterable<? extends ClassifiedEvent> trainingSet, Iterable<? extends ClassifiedEvent> testSet){
		
		Report combinedReport = null;
		
		// first convert iterable into memory incase these are lazy loaded. Do this so our memory inspection is most accurate
		
		if (!(trainingSet instanceof List)){
			trainingSet = Lists.newArrayList(trainingSet);
		}
		
		if (!(testSet instanceof List)){
			testSet = Lists.newArrayList(testSet);
		}
		
		for (int i = 0; i < numOfTests; i++){
			
			@SuppressWarnings("unchecked")
			Report report = runReport((List<ClassifiedEvent>)trainingSet, (List<ClassifiedEvent>)testSet);
			
			if (combinedReport == null){
				combinedReport = report;
			} else{
				combinedReport = combinedReport.avg(report);
			}
		}
		
		return combinedReport;
	}
	
	public Report getReport(Collection<? extends ClassifiedEvent> events, int numFolds){
		int foldSize = (int)Math.floor(events.size() / numFolds);
		
		Report combinedReport = null;
		
		for (int i = 0; i < numFolds; i++){
			
			Iterable<? extends ClassifiedEvent> trainingSet = getFold(i, foldSize, events);
			Iterable<? extends ClassifiedEvent> testSet = getInverseFold(i, foldSize, events);
			
			Report results = getReport(trainingSet, testSet);
			
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
		
		private Report(double accuracy, double fmeasure, double roc, long numBytesUsedForModel, int numTestsRun){
			this.accuracy = accuracy;
			this.fmeasure = fmeasure;
			this.roc = roc;
			this.numBytesUsedForModel = numBytesUsedForModel;
			this.numTestsRun = numTestsRun;
		}
		
		private Report avg(Report otherReport){
			return new Report((getAccuracy() + otherReport.getAccuracy()) / 2, (getFmeasure() + otherReport.getFmeasure()) / 2, (getRoc() + otherReport.getRoc()) / 2, (long)FastMath.floor((getNumBytesUsedForModel() + otherReport.getNumBytesUsedForModel()) / 2), getNumTestsRun() + otherReport.getNumTestsRun());
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
	}
}
