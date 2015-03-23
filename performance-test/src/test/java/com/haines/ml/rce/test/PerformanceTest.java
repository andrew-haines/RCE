package com.haines.ml.rce.test;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.service.ClassifierService;

public interface PerformanceTest {

	ClassifierService getClassifierService();

	void sendEvent(Event e);

	void notifyTrainingCompleted();
}
