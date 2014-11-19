package com.haines.ml.rce.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.test.model.TestEvent;
import com.haines.ml.rce.test.model.TestEvent.ClassificationProto;
import com.haines.ml.rce.test.model.TestEvent.FeatureProto;
import com.haines.ml.rce.window.WindowUpdatedListener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class RCEApplicationStartupTest {

	protected static final long DEFAULT_WINDOW_PERIOD = 1000;
	protected static final long DEFAULT_PUSH_DOWNSTREAM_MS = 200; // micro batch size
	private RCEApplication<TestEvent> candidate;
	private CountDownLatch started;
	private CountDownLatch finished;
	private CountDownLatch windowUpdated;
	private AtomicInteger eventsSeen;
	
	@Before
	public void before() throws RCEApplicationException, JAXBException, IOException{
		
		started = new CountDownLatch(1);
		finished = new CountDownLatch(1);
		windowUpdated = new CountDownLatch(3);
		eventsSeen = new AtomicInteger(0);
		
		RCEConfig defaultConfig = RCEConfig.UTIL.loadConfig(null);
		
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
				eventsSeen.incrementAndGet();
			}
		})
		.addSystemStartedListener(new WindowUpdatedListener() {
			
			@Override
			public void windowUpdated(NaiveBayesProbabilitiesProvider window) {
				windowUpdated.countDown();
			}
		})
		.setConfig(new RCEConfig.DefaultRCEConfig(defaultConfig){

			@Override
			public long getWindowPeriod() {
				return DEFAULT_WINDOW_PERIOD;
			}

			@Override
			public long getAsyncPushIntervalMs() {
				return DEFAULT_PUSH_DOWNSTREAM_MS;
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
		
		int eventNum;
		
		for (eventNum = 0; !windowUpdated.await(100, TimeUnit.MILLISECONDS); eventNum++){
			candidate.getEventConsumer().consume(getTestEvent(eventNum));
		}
		
		System.out.println("events recieved: "+eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
		
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
