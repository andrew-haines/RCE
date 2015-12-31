package com.haines.ml.rce.main;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.bind.JAXBException;

import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger LOG = LoggerFactory.getLogger(RCEApplicationStartupTest.class);
	
	protected static final long DEFAULT_WINDOW_PERIOD = 1000;
	protected static final long DEFAULT_PUSH_DOWNSTREAM_MS = 200; // micro batch size
	
	private static final ThreadLocal<IOSender> THREAD_LOCAL_SENDERS = new ThreadLocal<IOSender>();
	
	private static final Classification TEST_CLASS_1 = createClassification("true");
	private static final Classification TEST_CLASS_2 = createClassification("false");

	protected static final byte[] SUCCESS_PAYLOAD = new byte[]{127};
	
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
	
	protected RCEApplication<?> startUpRCE(FeatureHandlerRepositoryFactory repositoryFactory, RCEConfig config) throws InterruptedException, RCEApplicationException, JAXBException, IOException{
		started = new CountDownLatch(1);
		finished = new CountDownLatch(1);
		windowUpdated = new CountDownLatch(3);
		eventsSeen = new AtomicInteger(0);
		waitingForNextWindow = new AtomicBoolean(false);
		nextWindowUpdated = new CountDownLatch(1);
		
		this.rceConfig = config;
		
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
				LOG.info("Recieved event");
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
		
		return candidate;
	}
	
	public RCEApplication<?> startUpRCE(FeatureHandlerRepositoryFactory repositoryFactory) throws InterruptedException, RCEApplicationException, JAXBException, IOException{
		return startUpRCE(repositoryFactory, RCEConfig.UTIL.loadConfig());
	}
	
	protected boolean isUsingSlf4jEventListener() {
		return false;
	}

	public FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory() {
		return FeatureHandlerRepositoryFactory.ALL_DISCRETE_FEATURES;
	}

	@After
	public void after() throws RCEApplicationException, InterruptedException {
		
		shutdownAndWait();
		
		cleanThreadLocals(); // some nasty code here!
	}
	
	private void cleanThreadLocals() {
        try {
            // Get a reference to the thread locals table of the current thread
            Thread thread = Thread.currentThread();
            Field threadLocalsField = Thread.class.getDeclaredField("threadLocals");
            threadLocalsField.setAccessible(true);
            Object threadLocalTable = threadLocalsField.get(thread);

            // Get a reference to the array holding the thread local variables inside the
            // ThreadLocalMap of the current thread
            Class<?> threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Field tableField = threadLocalMapClass.getDeclaredField("table");
            tableField.setAccessible(true);
            Object table = tableField.get(threadLocalTable);

            // The key to the ThreadLocalMap is a WeakReference object. The referent field of this object
            // is a reference to the actual ThreadLocal variable
            Field referentField = Reference.class.getDeclaredField("referent");
            referentField.setAccessible(true);

            for (int i=0; i < Array.getLength(table); i++) {
                // Each entry in the table array of ThreadLocalMap is an Entry object
                // representing the thread local reference and its value
                Object entry = Array.get(table, i);
                if (entry != null) {
                    // Get a reference to the thread local object and remove it from the table
                    ThreadLocal<?> threadLocal = (ThreadLocal<?>)referentField.get(entry);
                    threadLocal.remove();
                }
            }
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }
	
	private com.haines.ml.rce.transport.Event getTestEvent(Classification classification, Feature... features) {
		
		com.haines.ml.rce.transport.Event event = new com.haines.ml.rce.transport.Event();
		
		event.setClassificationsList(Arrays.asList(classification));
		event.setFeaturesList(Lists.newArrayList(features));
		
		return event;
	}
	
	@Test
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticData_thenClassifierWorksAsExpected() throws IOException, InterruptedException, RCEApplicationException, JAXBException{
		startUpRCE(getFeatureHandlerRepositoryFactory());
		
		IOSender sender = getClientChannelProvider(rceConfig);
		
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("true", 1), getFeature("false", 2), getFeature("mild", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("no", 3), getFeature("false", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("false", 2), getFeature("strong", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("false", 2), getFeature("no", 3), getFeature("false", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("false", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)), sender);
		
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
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticDataOverMultipleWindows_thenClassifierWorksAsExpected() throws IOException, InterruptedException, RCEApplicationException, JAXBException{
		startUpRCE(getFeatureHandlerRepositoryFactory());
		
		IOSender sender = getClientChannelProvider(rceConfig);
		
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("true", 1), getFeature("false", 2), getFeature("mild", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("no", 3), getFeature("false", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("false", 2), getFeature("strong", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)), sender);
		
		Thread.sleep(50000);
		
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("false", 2), getFeature("no", 3), getFeature("false", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("true", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_2, getFeature("false", 1), getFeature("true", 2), getFeature("strong", 3), getFeature("false", 4)), sender);
		sendViaSelector(getTestEvent(TEST_CLASS_1, getFeature("true", 1), getFeature("true", 2), getFeature("mild", 3), getFeature("true", 4)), sender);
		
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
	public void givenCandidate_whenCallingStart_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException, JAXBException, IOException {
		startUpRCE(getFeatureHandlerRepositoryFactory());
		// add some test events through the system
		
		int eventNum;
		
		for (eventNum = 0; !windowUpdated.await(10, TimeUnit.MILLISECONDS); eventNum++){
			candidate.getEventConsumer().consume(getTestEvent(eventNum));
		}
		
		System.out.println("events recieved: "+eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
	}
	
	public void shutdownAndWait() throws RCEApplicationException, InterruptedException {
		
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
	public void givenCandidateAndPersistentTCPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException, IOException, JAXBException{
		
		startUpRCE(getFeatureHandlerRepositoryFactory(), getEventTransportDefinedConfig(StreamType.TCP));
		// add some test events through the system
		
		int eventNum;
		
		IOSender sender = getClientChannelProvider(rceConfig);
		
		//for (eventNum = 0; !windowUpdated.await(10, TimeUnit.MILLISECONDS); eventNum++){
		for (eventNum = 0; eventNum < 10; eventNum++){
			sendViaSelector(getTestEvent(eventNum), sender);
		}
		
		while(!windowUpdated.await(500, TimeUnit.MILLISECONDS) && eventsSeen.get() != eventNum); // busy wait until all events have been recieved
		
		LOG.info("events sent: {}",eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
	}
	
	@Test
	public void givenCandidateAndResettingTCPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException, IOException, JAXBException{
		
		startUpRCE(getFeatureHandlerRepositoryFactory(), getEventTransportDefinedConfig(StreamType.TCP));
		// add some test events through the system
		
		int eventNum;
		
		//for (eventNum = 0; !windowUpdated.await(10, TimeUnit.MILLISECONDS); eventNum++){
		for (eventNum = 0; eventNum < 10; eventNum++){
			IOSender sender = getTcpClientIOSender(rceConfig.getEventStreamSocketAddress(), true); // creates a new connection on each request
			sendViaSelector(getTestEvent(eventNum), sender);
		}
		
		while(!windowUpdated.await(500, TimeUnit.MILLISECONDS) && eventsSeen.get() != eventNum); // busy wait until all events have been recieved
		
		LOG.info("events sent: {}",eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
	}
	
	@Test
	public void givenCandidateAndUDPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly() throws RCEApplicationException, InterruptedException, IOException, JAXBException{
		startUpRCE(getFeatureHandlerRepositoryFactory(), getEventTransportDefinedConfig(StreamType.UDP));
		// add some test events through the system
		
		int eventNum;
		
		IOSender sender = getClientChannelProvider(rceConfig);
		
		for (eventNum = 0; !windowUpdated.await(10, TimeUnit.MILLISECONDS); eventNum++){
			
			sendViaSelector(getTestEvent(eventNum), sender);
		}
		
		System.out.println("events recieved: "+eventNum);
		assertThat(eventNum, is(equalTo(eventsSeen.get())));
	}
	
	protected static RCEConfig getEventTransportDefinedConfig(final StreamType streamType) throws JAXBException, IOException {
		return RCEConfig.UTIL.loadConfig(new RCEConfig.DefaultRCEConfig(null){

			@Override
			public StreamType getEventTransportProtocal() {
				return streamType;
			}
		});
	}

	public static <T extends Message<T>> void sendViaSelector(T testEvent, RCEConfig rceConfig, IOSender sender) throws IOException, InterruptedException {
		//LOG.debug("Sending event: "+event.testString1+"("+Integer.toBinaryString(event.testInt1)+"##"+event.testInt1+")");
		// dont need to worry about efficiency in test case...
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		ProtostuffIOUtil.writeTo(out, testEvent, testEvent.cachedSchema(), LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
		
		out.flush();
		out.close();
		
		sender.write(ByteBuffer.wrap(out.toByteArray()));
	}
	
	protected <T extends Message<T>> void sendViaSelector(T testEvent, IOSender sender) throws IOException, InterruptedException {
		sendViaSelector(testEvent, rceConfig, sender);
	}
	
	public static IOSender getClientChannelProvider(RCEConfig config) throws IOException, InterruptedException{
		
		IOSender threadIoSender = THREAD_LOCAL_SENDERS.get();
		
		if (threadIoSender == null){ // create a new connection for this thread
			switch (config.getEventTransportProtocal()){
				case UDP:
					threadIoSender = getUdpClientIOSender(config.getEventStreamSocketAddress());
					break;
			    case TCP:
			    	threadIoSender = getTcpClientIOSender(config.getEventStreamSocketAddress(), false);
			    	break;
				
				default: throw new IllegalArgumentException("Unknown stream type: "+config.getEventTransportProtocal());
			}
			
			THREAD_LOCAL_SENDERS.set(threadIoSender);
		}
		return threadIoSender;
	}

	private static IOSender getTcpClientIOSender(SocketAddress eventStreamSocketAddress, final boolean closeAfterEachInvocation) throws IOException {
		
		LOG.info("creating TCP client channel provider for {}", eventStreamSocketAddress);
		
		final Selector selector = SelectorProvider.provider().openSelector();
		
		final SocketChannel serverChannel = initiateConnection(eventStreamSocketAddress, selector);
		
//		final SelectionKey writeKey = connectedChannel.keyFor(selector);
//		writeKey.interestOps(SelectionKey.OP_WRITE);
		
		final SelectionKey readKey = serverChannel.keyFor(selector);
		readKey.interestOps(SelectionKey.OP_READ);
//		
//		final SelectionKey acceptKey = connectedChannel.keyFor(selector);
//		acceptKey.interestOps(SelectionKey.OP_ACCEPT);
		
		final ByteBuffer readBuffer = ByteBuffer.allocate(2048);
		
		return new IOSender(){

			@Override
			public void write(ByteBuffer data) throws IOException {
				
				SocketChannel connectedChannel = serverChannel;
				if (!connectedChannel.isOpen()){
					throw new IllegalStateException("channel not open");
				}
				connectedChannel.write(data);
				LOG.info("sent event");

				if (selector.select() == 0){
					throw new IllegalStateException("Should have recieved a response message");
				}; // specificly wait for the completed packet to come back
				
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				
				while (keys.hasNext()){
					SelectionKey key = keys.next();
					keys.remove();
					
					LOG.debug("Recieved Key for: readable("+key.isReadable()+"), writable("+key.isWritable()+"), connectable("+key.isConnectable()+"), acceptable("+key.isAcceptable()+")");
					SocketChannel socketChannel = (SocketChannel) key.channel();
					
					if (key.isReadable()){
						readBuffer.clear();
						socketChannel.read(readBuffer);
						
						readBuffer.flip();
						if (readBuffer.get() != 127 || readBuffer.position() != 1){ // success byte
							throw new IllegalStateException("response was not a success message: "+Arrays.toString(readBuffer.array()));
						}
						
						// now wait for the response from the server
						
						if (closeAfterEachInvocation){
							LOG.info("closing connection to server");
							socketChannel.close();
							serverChannel.close();
						}
					} 
				}
			}
			
		};
	}
	
	private static SocketChannel initiateConnection(SocketAddress eventStreamSocketAddress, Selector selector) throws IOException {
		
		LOG.info("Creating a new connection to server: "+eventStreamSocketAddress);
		// Create a non-blocking socket channel
		SocketChannel socketChannel = SocketChannel.open();
		configureChannel(socketChannel);
	
		// Kick off connection establishment
		socketChannel.connect(eventStreamSocketAddress);
	
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
		
		while(selector.select() != 1){}; // busy spin until connection is made
		
		Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
		if (!selectedKeys.next().isConnectable()){
			throw new IllegalStateException("We should have recieved a connection response");
		} else{
			selectedKeys.remove();
			if (selectedKeys.hasNext()){
				throw new IllegalStateException("There are still selections requiring attention. ");
			}
		}
		if (!socketChannel.finishConnect()){
			throw new IllegalStateException("unable to finish connection");
		}
		
		if (!socketChannel.isConnected()){
			throw new IllegalStateException("connection not established");
		}
		
		return socketChannel;
	}


	protected static IOSender getUdpClientIOSender(final SocketAddress address) throws IOException, InterruptedException {
		final DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
		
		channel.connect(address);
		
		LOG.info("created UDP client channel at {}", address);
		
		return new IOSender(){

			@Override
			public void write(ByteBuffer data) throws IOException {
				channel.send(data, address);
			}
			
		};
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
	
	public interface IOSender {
		
		public void write(ByteBuffer data) throws IOException;
	}
	
	private static void configureChannel(SocketChannel channel) throws IOException {
	    channel.configureBlocking(false);
	    channel.socket().setSendBufferSize(0x100000); // 1Mb
	    channel.socket().setReceiveBufferSize(0x100000); // 1Mb
	    channel.socket().setKeepAlive(true);
	    channel.socket().setReuseAddress(true);
	    channel.socket().setSoLinger(false, 0);
	    channel.socket().setSoTimeout(0);
	    channel.socket().setTcpNoDelay(true);
	}
}
