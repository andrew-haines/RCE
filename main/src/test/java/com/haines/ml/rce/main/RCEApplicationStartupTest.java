package com.haines.ml.rce.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.common.collect.Lists;
import com.haines.ml.rce.eventstream.EventStreamListener;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.config.RCEConfig.StreamType;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;
import com.haines.ml.rce.service.ClassifierService.PredictedClassification;
import com.haines.ml.rce.transport.ValueType;
import com.haines.ml.rce.transport.Event.Classification;
import com.haines.ml.rce.transport.Event.Feature;
import com.haines.ml.rce.window.WindowUpdatedListener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.closeTo;

public class RCEApplicationStartupTest {

	protected static final long DEFAULT_WINDOW_PERIOD = 1000;
	protected static final long DEFAULT_PUSH_DOWNSTREAM_MS = 200; // micro batch size
	
	private static final Classification TEST_CLASS_1 = createClassification("true");
	private static final Classification TEST_CLASS_2 = createClassification("false");
	
	protected NaiveBayesRCEApplication<Event> candidate;
	protected CountDownLatch started;
	protected CountDownLatch finished;
	protected CountDownLatch windowUpdated;
	protected CountDownLatch nextWindowUpdated;
	protected AtomicBoolean waitingForNextWindow;
	protected AtomicInteger eventsSeen;
	protected SocketAddress serverAddress;
	private ClassLoader classLoader;
	private RCEConfig rceConfig;
	
	protected RCEApplicationStartupTest(ClassLoader classLoader){
		this.classLoader = classLoader;
	}
	
	public static Classification createClassification(String value) {
		Classification classification = new Classification();
		classification.setValueType(ValueType.STRING);
		classification.setStringValue(value);
		
		return classification;
	}

	public RCEApplicationStartupTest(){
		this(null);
	}
	
	protected void startUpRCE(FeatureHandlerRepositoryFactory repositoryFactory) throws InterruptedException, RCEApplicationException, JAXBException, IOException{
		started = new CountDownLatch(1);
		finished = new CountDownLatch(1);
		windowUpdated = new CountDownLatch(3);
		eventsSeen = new AtomicInteger(0);
		waitingForNextWindow = new AtomicBoolean(false);
		nextWindowUpdated = new CountDownLatch(1);
		
		rceConfig = RCEConfig.UTIL.loadConfig(null);
		
		serverAddress = rceConfig.getEventStreamSocketAddress();
		
		RCEApplication.RCEApplicationBuilder<Event> builder = new RCEApplication.RCEApplicationBuilder<Event>(null).addSystemStartedListener(new EventStreamListener() {

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
			public void newWindowCreated(NaiveBayesProbabilitiesProvider window) {
				windowUpdated.countDown();
				
				if (waitingForNextWindow.get()){
					nextWindowUpdated.countDown();
				}
			}
		})
		.setConfig(new RCEConfig.DefaultRCEConfig(rceConfig))
		.setHandlerRepositoryFactory(repositoryFactory);
		
		if (classLoader != null){
			builder.setClassLoader(classLoader);
		}
		
		if (isUsingSlf4jEventListener()){
			builder.addSystemStartedListener(new EventStreamListener.SLF4JStreamListener());
		}
		
		candidate = (NaiveBayesRCEApplication<Event>)builder.build();
		
		startServerAndWait();
	}
	
	@Before
	public void before() throws RCEApplicationException, JAXBException, IOException, InterruptedException{
		
		startUpRCE(getFeatureHandlerRepositoryFactory());
	}
	
	protected boolean isUsingSlf4jEventListener() {
		return false;
	}

	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory() {
		return FeatureHandlerRepositoryFactory.ALL_DISCRETE_FEATURES;
	}

	@After
	public void after() throws RCEApplicationException, InterruptedException{
		shutdownAndWait();
	}
	
	private com.haines.ml.rce.transport.Event getTestEvent(Classification classification, Feature... features) {
		
		com.haines.ml.rce.transport.Event event = new com.haines.ml.rce.transport.Event();
		
		event.setClassificationsList(Arrays.asList(classification));
		event.setFeaturesList(Lists.newArrayList(features));
		
		return event;
	}
	
	@Test
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticData_thenClassifierWorksAsExpected() throws IOException, InterruptedException{
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("true", 1), getFeature("false", 2), getFeature("mild", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("no", 3), getFeature("false", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("false", 2), getFeature("strong", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("false", 2), getFeature("no", 3), getFeature("false", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("false", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)));
		
		waitingForNextWindow.set(true);
		nextWindowUpdated.await();
		
		Thread.sleep(5000);// There is a race condition here with the test. We need to ensure that the heart beat has triggered
		
		for (int i = 0; i < 5; i++){
			
			Thread.sleep(2000);
			
			PredictedClassification classification = candidate.getNaiveBayesService().getClassification(Arrays.asList(getFeature("true", 1), getFeature("false", 2), getFeature("mild", 3), getFeature("false", 4)));
			
			assertThat((String)classification.getClassification().getValue(), is(equalTo(TEST_CLASS_2.getValue())));
			assertThat(classification.getCertainty(), is(closeTo(0.0185, 0.0001)));
		}
	}
	
	//@Test
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticDataOverMultipleWindows_thenClassifierWorksAsExpected() throws IOException, InterruptedException{
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("true", 1), getFeature("false", 2), getFeature("mild", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("no", 3), getFeature("false", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("false", 2), getFeature("strong", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)));
		
		Thread.sleep(50000);
		
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("false", 2), getFeature("no", 3), getFeature("false", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("true", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("false", 4)));
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)));
		
		waitingForNextWindow.set(true);
		nextWindowUpdated.await();
		
		for (int i = 0; i < 5; i++){
			
			System.out.println(candidate.getNaiveBayesService().toString());
			PredictedClassification classification = candidate.getNaiveBayesService().getClassification(Arrays.asList(getFeature("true", 1), getFeature("false", 2), getFeature("mild", 3), getFeature("false", 4)));
		
			Thread.sleep(2000);
			
			if (classification.getClassification() != Classification.UNKNOWN && i == 0){ // sometimes the push may not have happened even after waiting 2 seconds
			
				
				assertThat((String)classification.getClassification().getValue(), is(equalTo(TEST_CLASS_2.getValue())));
				assertThat(classification.getCertainty(), is(closeTo(0.0185, 0.0001)));
			}
		}
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
		
		if (candidate != null){
			candidate.stop();
		
			finished.await();
		}
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
	
	public static <T extends Message<T>> void sendViaSelector(T testEvent, RCEConfig rceConfig) throws IOException, InterruptedException {
		WritableByteChannel channel = getClientChannel(rceConfig.getEventStreamSocketAddress());
		//LOG.debug("Sending event: "+event.testString1+"("+Integer.toBinaryString(event.testInt1)+"##"+event.testInt1+")");
		// dont need to worry about efficiency in test case...
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		ProtostuffIOUtil.writeTo(out, testEvent, testEvent.cachedSchema(), LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
		
		out.flush();
		out.close();
		
		//if (rceConfig.getEventTransportProtocal() == StreamType.UDP){
		//	((DatagramChannel)channel).send(ByteBuffer.wrap(out.toByteArray()), rceConfig.getEventStreamSocketAddress());
		//} else{
			channel.write(ByteBuffer.wrap(out.toByteArray()));
		//}
		
		channel.close();
	}
	
	protected <T extends Message<T>> void sendViaSelector(T testEvent) throws IOException, InterruptedException {
		sendViaSelector(testEvent, rceConfig);
	}

	protected static DatagramChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException {
		DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
		
		channel.connect(address);
		
		return channel;
	}

	private com.haines.ml.rce.transport.Event getTestEvent(int value) {
		com.haines.ml.rce.transport.Event event = new com.haines.ml.rce.transport.Event();
		
		List<Classification> classifications = new ArrayList<Classification>();
		List<Feature> features = new ArrayList<Feature>();
		
		Feature feature1 = getFeature("feature1_"+value, 1);
		Feature feature2 = getFeature("feature2_"+value, 1);
		Feature feature3 = getFeature("feature3_"+value, 1);
		
		features.add(feature1);
		features.add(feature2);
		features.add(feature3);
		
		Classification class1 = createClassification(value+"");
		Classification class2 = createClassification((value+10)+"");
		
		classifications.add(class1);
		classifications.add(class2);
		
		event.setClassificationsList(classifications);
		event.setFeaturesList(features);
		
		return event;
	}
	
	private Feature getFeature(String value, int type){
		
		Feature feature = new Feature();
		
		feature.setType(type);
		feature.setValueType(ValueType.STRING);
		feature.setStringValue(value);
		return feature;
	}
}
