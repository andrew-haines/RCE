package com.haines.ml.rce.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.test.model.TestEvent;
import com.haines.ml.rce.test.model.TestEvent.ClassificationProto;
import com.haines.ml.rce.test.model.TestEvent.FeatureProto;

public class RCEApplicationStartupTest {

	private RCEApplication<TestEvent> candidate;
	private CountDownLatch started;
	private CountDownLatch finished;
	
	@Before
	public void before() throws RCEApplicationException{
		
		started = new CountDownLatch(1);
		finished = new CountDownLatch(1);
		
		candidate = new RCEApplication.RCEApplicationBuilder<TestEvent>(null).addSystemStartedListener(new EventStreamListener() {

			@Override
			public void streamStarted() {
				started.countDown();
			}

			@Override
			public void streamStopped() {
				finished.countDown();
			}

			@Override
			public void recievedEvent(Event event) {
				// NO OP
			}
		}).build();
	}
	
	@Test
	public void givenCandidate_whenCallingStart_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException{
		
		
		Thread server = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					candidate.start();
				} catch (RCEApplicationException e) {
					throw new RuntimeException("Unable to start test", e);
				}
			}
			
		});
		
		server.start();
		
		started.await();
		
		// add some test events through the system
		
		candidate.getEventConsumer().consume(getTestEvent(1));
		candidate.getEventConsumer().consume(getTestEvent(2));
		candidate.getEventConsumer().consume(getTestEvent(3));
		candidate.getEventConsumer().consume(getTestEvent(4));
		
		// now stop the system
		
		candidate.stop();
		
		finished.await();
	}

	private TestEvent getTestEvent(int value) {
		TestEvent event = new TestEvent();
		
		List<ClassificationProto> classifications = new ArrayList<ClassificationProto>();
		List<FeatureProto> features = new ArrayList<FeatureProto>();
		
		FeatureProto feature1 = new FeatureProto("feature1_"+value);
		FeatureProto feature2 = new FeatureProto("feature2_"+value);
		FeatureProto feature3 = new FeatureProto("feature3_"+value);
		
		features.add(feature1);
		features.add(feature2);
		features.add(feature3);
		
		ClassificationProto class1 = new ClassificationProto(value+"");
		ClassificationProto class2 = new ClassificationProto((value+10)+"");
		
		classifications.add(class1);
		classifications.add(class2);
		
		event.setClassificationsList(classifications);
		event.setFeaturesList(features);
		
		return event;
	}
}
