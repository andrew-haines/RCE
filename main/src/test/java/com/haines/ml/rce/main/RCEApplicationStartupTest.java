package com.haines.ml.rce.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;
import com.haines.ml.rce.transport.Event;
import com.haines.ml.rce.transport.Event.Classification;
import com.haines.ml.rce.transport.Event.Feature;
import com.haines.ml.rce.window.WindowUpdatedListener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class RCEApplicationStartupTest {

	protected static final long DEFAULT_WINDOW_PERIOD = 1000;
	protected static final long DEFAULT_PUSH_DOWNSTREAM_MS = 200; // micro batch size
	protected NaiveBayesRCEApplication<Event> candidate;
	protected CountDownLatch started;
	protected CountDownLatch finished;
	protected CountDownLatch windowUpdated;
	protected CountDownLatch nextWindowUpdated;
	protected AtomicBoolean waitingForNextWindow;
	protected AtomicInteger eventsSeen;
	protected SocketAddress serverAddress;
	
	@Before
	public void before() throws RCEApplicationException, JAXBException, IOException, InterruptedException{
		
		started = new CountDownLatch(1);
		finished = new CountDownLatch(1);
		windowUpdated = new CountDownLatch(3);
		eventsSeen = new AtomicInteger(0);
		waitingForNextWindow = new AtomicBoolean(false);
		nextWindowUpdated = new CountDownLatch(1);
		
		RCEConfig defaultConfig = RCEConfig.UTIL.loadConfig(null);
		
		serverAddress = defaultConfig.getEventStreamSocketAddress();
		
		candidate = (NaiveBayesRCEApplication<Event>)new RCEApplication.RCEApplicationBuilder<Event>(null).addSystemStartedListener(new EventStreamListener() {

			@Override
			public void streamStarted() {
				started.countDown();
			}

			@Override
			public void streamStopped() {
				finished.countDown();
			}

			@Override
			public void recievedEvent(com.haines.ml.rce.model.Event event) {
				eventsSeen.incrementAndGet();
			}
		})
		.addSystemStartedListener(new WindowUpdatedListener() {
			
			@Override
			public void windowUpdated(NaiveBayesProbabilitiesProvider window) {
				windowUpdated.countDown();
				
				if (waitingForNextWindow.get()){
					nextWindowUpdated.countDown();
				}
			}
		})
		.setConfig(new RCEConfig.DefaultRCEConfig(defaultConfig){

			@Override
			public long getWindowPeriod() {
				return DEFAULT_WINDOW_PERIOD;
			}

			@Override
			public long getMicorBatchIntervalMs() {
				return DEFAULT_PUSH_DOWNSTREAM_MS;
			}
			
			
		}).build();
		
		startServerAndWait();
	}
	
	@After
	public void after() throws RCEApplicationException, InterruptedException{
		shutdownAndWait();
	}
	
	@Test
	public void givenCandidate_whenCallingStart_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException {
		
		// add some test events through the system
		
		int eventNum;
		
		for (eventNum = 0; !windowUpdated.await(10, TimeUnit.MILLISECONDS); eventNum++){
			candidate.getEventConsumer().consume(getTestEvent(eventNum));
		}
		
		System.out.println("events recieved: "+eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
	}
	
	private void shutdownAndWait() throws RCEApplicationException, InterruptedException {
		
		// now stop the system
		
		candidate.stop();
		
		finished.await();
	}

	protected void startServerAndWait() throws InterruptedException{
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
	}
	
	@Test
	public void givenCandidate_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException, IOException{
		
		// add some test events through the system
		
		int eventNum;
		
		for (eventNum = 0; !windowUpdated.await(10, TimeUnit.MILLISECONDS); eventNum++){
			
			sendViaSelector(getTestEvent(eventNum));
		}
		
		System.out.println("events recieved: "+eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
	}
	
	protected void sendViaSelector(Event testEvent) throws IOException, InterruptedException {
		DatagramChannel channel = getClientChannel(serverAddress);
		//LOG.debug("Sending event: "+event.testString1+"("+Integer.toBinaryString(event.testInt1)+"##"+event.testInt1+")");
		// dont need to worry about efficiency in test case...
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		ProtostuffIOUtil.writeTo(out, testEvent, testEvent.cachedSchema(), LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
		
		out.flush();
		out.close();
		
		
		channel.send(ByteBuffer.wrap(out.toByteArray()), serverAddress);
		
		channel.close();
	}

	protected DatagramChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException {
		DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
		
		channel.connect(address);
		
		return channel;
	}

	private Event getTestEvent(int value) {
		Event event = new Event();
		
		List<Classification> classifications = new ArrayList<Classification>();
		List<Feature> features = new ArrayList<Feature>();
		
		Feature feature1 = getFeature("feature1_"+value, 1);
		Feature feature2 = getFeature("feature2_"+value, 1);
		Feature feature3 = getFeature("feature3_"+value, 1);
		
		features.add(feature1);
		features.add(feature2);
		features.add(feature3);
		
		Classification class1 = new Classification(value+"");
		Classification class2 = new Classification((value+10)+"");
		
		classifications.add(class1);
		classifications.add(class2);
		
		event.setClassificationsList(classifications);
		event.setFeaturesList(features);
		
		return event;
	}
	
	private Feature getFeature(String value, int type){
		
		Feature feature = new Feature();
		
		feature.setType(type);
		feature.setValue(value);
		return feature;
	}
}
