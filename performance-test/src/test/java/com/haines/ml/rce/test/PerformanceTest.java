package com.haines.ml.rce.test;

import java.util.List;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.service.ClassifierService;
import com.haines.ml.rce.service.ClassifierService.RandomisedClassifierService;

public interface PerformanceTest {

	ClassifierService getClassifierService();

	void sendEvent(Event e);

	void notifyTrainingCompleted();

	void reset();
	
	public static class RandomPerformanceTest implements PerformanceTest {

		private final RandomisedClassifierService randomService;
		
		public RandomPerformanceTest(List<? extends Classification> possibleClassifications){
			this.randomService = new RandomisedClassifierService(possibleClassifications);
		}
		
		@Override
		public ClassifierService getClassifierService() {
			return randomService;
		}

		@Override
		public void sendEvent(Event e) {
			// no op
		}

		@Override
		public void notifyTrainingCompleted() {
			// no op
		}

		@Override
		public void reset() {
			// no op
		}
		
	}
}
