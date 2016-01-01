package com.haines.ml.rce.eventstream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.client.IOSender;
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.dispatcher.DispatcherConsumer;
import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.UnMarshalableException;
import com.haines.ml.rce.model.system.Clock;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public abstract class AbstractSelectorEventStreamIT<T extends SelectableChannel & NetworkChannel, C extends WritableByteChannel> {

	private static final String TEST_EVENT_MESSAGE  = "This is a test event";
	private static final int TEST_EVENT_ID = 257; // 0x101
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSelectorEventStreamIT.class);
	
	public static final int TEST_PORT = 34564;
	private static final int NUMBER_EVENTS_TO_SEND = 16000;
	private SelectorEventStream<T, TestEvent> candidate;
	private Executor executor;
	private TestDispatcher dispatcher;
	private CountDownLatch startupLatch;
	private CountDownLatch shutdownLatch;
	private SelectorEventStreamConfig config;
	
	@Before
	public void before() throws UnknownHostException{
		before(1);
	}
	
	protected int getNumberOfEvents(){
		return NUMBER_EVENTS_TO_SEND;
	}
	public void before(int eventsExpected) throws UnknownHostException{
		
		executor = Executors.newSingleThreadExecutor();
		dispatcher = new TestDispatcher(eventsExpected);
		
		NetworkChannelProcessor<T> processor = createNetworkChannelProcessor();
		
		config = new SelectorEventStreamConfig.SelectorEventStreamConfigBuilder()
		.bufferCapacity(getBufferCapacity())
		.bufferType(BufferType.DIRECT_BUFFER)
		.socketAddress(new InetSocketAddress(InetAddress.getLoopbackAddress(), TEST_PORT))
		.build();
		
		startupLatch = new CountDownLatch(1);
		shutdownLatch = new CountDownLatch(1);
		
		SelectorEventStreamFactory<T, TestEvent> streamFactory = new SelectorEventStreamFactory<T, TestEvent>(Clock.SYSTEM_CLOCK, config, processor, new TestEventBuffer(), new LatchNotifierEventStreamListener(startupLatch, shutdownLatch));
		candidate = streamFactory.create(dispatcher);
	}
	
	abstract protected int getBufferCapacity();
	
	@After
	public void after() throws EventStreamException, InterruptedException{
		candidate.stop();
		shutdownLatch.await();
	}
	
	protected abstract NetworkChannelProcessor<T> createNetworkChannelProcessor();
	
	@Test
	public void givenCandidate_whenCallingStart_thenStreamIsAlive() throws EventStreamException, InterruptedException{
		assertThat(candidate.isAlive(), is(equalTo(false)));
		
		executor.execute(getStarter(candidate));
		
		startupLatch.await();
		assertThat(candidate.isAlive(), is(equalTo(true)));
	}
	
	@Test
	public void givenCandidate_whenCallingStop_thenStreamIsNotAlive() throws EventStreamException, InterruptedException{
		assertThat(candidate.isAlive(), is(equalTo(false)));
		
		executor.execute(getStarter(candidate));
		
		startupLatch.await();
		assertThat(candidate.isAlive(), is(equalTo(true)));
		
		candidate.stop();
		
		shutdownLatch.await();
		
		assertThat(candidate.isAlive(), is(equalTo(false)));
	}
	
	@Test
	public void givenStartedCandidate_whenPushingEventToPort_thenDispatcherIsInvokedWithEvent() throws InterruptedException, IOException{
		
		// start server
		executor.execute(getStarter(candidate));
		
		//await startup to complete
		startupLatch.await();
		
		IOSender sender = getIOSender(config.getAddress());
		
		// sendmessage
		
		sendEventToServer(new TestEvent(TEST_EVENT_MESSAGE, TEST_EVENT_ID), sender);
		
		dispatcher.waitForEvents();
		// now verify that the dispatcher recieved the event
		
		Iterable<TestEvent> events = dispatcher.getEventsRecieved();
		
		assertThat(Iterables.size(events), is(equalTo(1)));
		
		assertThat(Iterables.get(events, 0).testString1, is(equalTo(TEST_EVENT_MESSAGE)));
		assertThat(Iterables.get(events, 0).testInt1, is(equalTo(TEST_EVENT_ID)));
	}
	
	@Test
	public void givenStartedCandidate_whenPushingMultipleEventsToPort_thenDispatcherIsInvokedWithAllEvents() throws InterruptedException, IOException{
		
		before(getNumberOfEvents());
		// start server
		executor.execute(getStarter(candidate));
		
		//await startup to complete
		startupLatch.await();
		
		IOSender sender = getIOSender(config.getAddress());
		
		// sendmessages
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < getNumberOfEvents(); i++){
			sendEventToServer(new TestEvent(TEST_EVENT_MESSAGE, i), sender);
		}
		
		dispatcher.waitForEvents();
		// now verify that the dispatcher recieved the event
		
		LOG.debug("Finished Recieving events");
		
		long timeSpent = System.currentTimeMillis() - startTime;
		LOG.debug("Sent "+getNumberOfEvents()+" in "+(timeSpent)+"ms - "+calculateRPS(timeSpent, getNumberOfEvents())+" rps");
		
		Iterable<TestEvent> events = dispatcher.getEventsRecieved();
		
		assertThat(Iterables.size(events), is(equalTo(getNumberOfEvents())));
		
		int idx = 0;
		for (TestEvent event:events){
			assertThat(event.testString1, is(equalTo(TEST_EVENT_MESSAGE)));
			assertThat(event.testInt1, is(equalTo(idx++)));
		}
	}
	
	private static double calculateRPS(long timeSpent, int numberEventsToSend) {
		
		return 1000 / ((double)timeSpent / numberEventsToSend);
	}
	private void sendEventToServer(TestEvent event, IOSender sender) throws IOException, InterruptedException{

		//LOG.debug("Sending event: "+event.testString1+"("+Integer.toBinaryString(event.testInt1)+"##"+event.testInt1+")");
		// dont need to worry about efficiency in test case...
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(event.testString1.getBytes());
		out.write(Character.CONTROL);
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		
		byteBuffer.putInt(event.testInt1);
		out.write(byteBuffer.array());
		
		out.flush();
		out.close();
		
		sendBytes(sender, ByteBuffer.wrap(out.toByteArray()));
	}
	
	protected void sendBytes(IOSender sender, ByteBuffer buffer) throws IOException{
		
		sender.write(buffer);
	}
	
	protected abstract IOSender getIOSender(SocketAddress address) throws IOException, InterruptedException;
	
	private static Runnable getStarter(final SelectorEventStream<?, TestEvent> candidate){
		return new Runnable(){

			@Override
			public void run() {
				try {
					candidate.start();
				} catch (EventStreamException e) {
					throw new RuntimeException("Unable to start stream", e);
				}
			}
		};
	}
	
	private static class TestEvent implements Event{
		
		private final String testString1;
		private final int testInt1;
		
		private TestEvent(String testString1, int testInt1){
			this.testString1 = testString1;
			this.testInt1 = testInt1;
		}
	}
	
	private static class TestEventBuffer implements EventMarshalBuffer<TestEvent>{

		private static enum TestEventProperty{
			TEST_STRING1,
			TEST_INT_1
		}
		private StringBuilder testString1 = new StringBuilder();
		private ByteBuffer testInt1 = ByteBuffer.allocate(5);
		private byte lastRead;
		private int testIntIdx = 0;
		
		private TestEventProperty currentProperty = TestEventProperty.TEST_STRING1;
		
		@Override
		public boolean marshal(ByteBuffer content) {
			
			while(content.hasRemaining() && testIntIdx != 4){
				
				byte nextByte = content.get();
	
				if (currentProperty == TestEventProperty.TEST_STRING1 && lastRead == Character.CONTROL){ // 2 byte delimiter. Used as an example for when we might need look back
					currentProperty = TestEventProperty.TEST_INT_1;
				}
				
				switch(currentProperty){
					case TEST_INT_1 :{
						
						//testInt1 |= (nextByte << (testIntIdx * 8)); // shift by 8 bits for each byte
						testInt1.put(nextByte);
						testIntIdx++;
						break;
					}
					case TEST_STRING1:{
						if (nextByte != Character.CONTROL){
							testString1.append((char)nextByte);
						}
						break;
					}
				}
				lastRead  = nextByte;
			}
			
			return testIntIdx == 4;
		}

		@Override
		public TestEvent buildEventAndResetBuffer() throws UnMarshalableException {
			
			String testString1 = this.testString1.toString();
			testInt1.flip();
			int testInt1 = this.testInt1.getInt();
			
			this.testInt1.clear();
			this.testIntIdx = 0;
			this.testString1.setLength(0); // reset string buffer
			this.lastRead = 0;
			this.currentProperty = TestEventProperty.TEST_STRING1;
			return new TestEvent(testString1.toString(), testInt1);
		}
	}
	
	private static class TestDispatcher extends Dispatcher<TestEvent>{
		
		private volatile Collection<TestEvent> events = Collections.synchronizedCollection(new ArrayList<TestEvent>());
		private CountDownLatch latch;
		
		private TestDispatcher(int numberEventsExpected){
			super(Collections.<DispatcherConsumer<TestEvent>>emptyList());
			LOG.debug("expecting {} events", numberEventsExpected);
			this.latch = new CountDownLatch(numberEventsExpected);
		}
		
		@Override
		public void dispatchEvent(TestEvent event) {
			
			this.events.add(event);
			
			latch.countDown();
			//LOG.debug("recieved event: "+event.testString1+"("+Integer.toBinaryString(event.testInt1)+"##"+event.testInt1+")");
		}

		public void waitForEvents() throws InterruptedException {
			latch.await();
		}

		public Iterable<TestEvent> getEventsRecieved() {
			return events;
		}
	}
	
	private static class LatchNotifierEventStreamListener implements EventStreamListener{

		private final CountDownLatch startupLatch;
		private final CountDownLatch shutdownLatch;
		
		private LatchNotifierEventStreamListener(CountDownLatch startupLatch, CountDownLatch shutdownLatch){
			this.startupLatch = startupLatch;
			this.shutdownLatch = shutdownLatch;
		}
		
		@Override
		public void streamStarted() {
			startupLatch.countDown();
		}

		@Override
		public void streamStopped() {
			shutdownLatch.countDown();
		}

		@Override
		public void recievedEvent(Event event) {
			// NO OP
		}
		
	}
}
