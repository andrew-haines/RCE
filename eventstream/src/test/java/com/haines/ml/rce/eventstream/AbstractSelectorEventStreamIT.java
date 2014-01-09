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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventBuffer;
import com.haines.ml.rce.model.UnMarshalableException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public abstract class AbstractSelectorEventStreamIT<T extends SelectableChannel & NetworkChannel> {

	private static final String TEST_EVENT_MESSAGE  = "This is a test event";
	private static final int TEST_EVENT_ID = 257; // 0x101
	
	private static final int TEST_PORT = 45322;
	private SelectorEventStream<T> candidate;
	private Executor executor;
	private TestDispatcher dispatcher;
	private CountDownLatch startupLatch;
	private CountDownLatch shutdownLatch;
	private SelectorEventStreamConfig config;
	
	@Before
	public void before() throws UnknownHostException{
		
		executor = Executors.newSingleThreadExecutor();
		dispatcher = new TestDispatcher(1);
		
		config = new SelectorEventStreamConfig.SelectorEventStreamConfigBuilder()
		.bufferCapacity(3)
		.socketAddress(new InetSocketAddress(InetAddress.getLocalHost(), TEST_PORT))
		.build();
		
		startupLatch = new CountDownLatch(1);
		shutdownLatch = new CountDownLatch(1);
		
		SelectorEventStreamFactory<T> streamFactory = new SelectorEventStreamFactory<T>(config, createNetworkChannelProcessor(), new TestEventBuffer(), new LatchNotifierEventStreamListener(startupLatch, shutdownLatch));
		candidate = streamFactory.create(dispatcher);
	}
	
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
		
		// sendmessage
		
		sendEventToServer(new TestEvent(TEST_EVENT_MESSAGE, TEST_EVENT_ID));
		
		dispatcher.waitForEvent(1);
		// now verify that the dispatcher recieved the event
		
		Iterable<TestEvent> events = dispatcher.getEventsRecieved();
		
		assertThat(Iterables.size(events), is(equalTo(1)));
		
		System.out.println(Arrays.toString(Iterables.get(events, 0).testString1.getBytes()));
		System.out.println(Arrays.toString(TEST_EVENT_MESSAGE.getBytes()));
		assertThat(Iterables.get(events, 0).testString1, is(equalTo(TEST_EVENT_MESSAGE)));
		assertThat(Iterables.get(events, 0).testInt1, is(equalTo(TEST_EVENT_ID)));
	}
	
	private void sendEventToServer(TestEvent event) throws IOException, InterruptedException{
		
		WritableByteChannel channel = getClientChannel(config.getAddress());
		
		// dont need to worry about efficiency in test case...
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(event.testString1.getBytes());
		out.write(Character.CONTROL);
		
		out.write(event.testInt1 & 0xFF);
		out.write((event.testInt1 & (0xFF00)) >> 8);
		out.write((event.testInt1 & (0xFF0000)) >> 16);
		out.write((event.testInt1 & (0xFF000000)) >> 24);
		out.write((event.testInt1 & (0xFF000000)) >> 24);
		
		out.flush();
		
		channel.write(ByteBuffer.wrap(out.toByteArray()));
		
		channel.close();
	}
	
	protected abstract WritableByteChannel getClientChannel(SocketAddress address) throws IOException, InterruptedException;
	
	private static Runnable getStarter(final SelectorEventStream<?> candidate){
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
	
	private static class TestEventBuffer implements EventBuffer<TestEvent>{

		private static enum TestEventProperty{
			TEST_STRING1,
			TEST_INT_1
		}
		private StringBuilder testString1 = new StringBuilder();
		private int testInt1;
		private byte lastRead;
		private int testIntIdx = 0;
		
		private TestEventProperty currentProperty = TestEventProperty.TEST_STRING1;
		
		@Override
		public void marshal(ByteBuffer content) {
			while(content.hasRemaining()){
				byte nextByte = content.get();
	
				if (currentProperty == TestEventProperty.TEST_STRING1 && lastRead == Character.CONTROL){ // 2 byte delimiter. Used as an example for when we might need look back
					currentProperty = TestEventProperty.TEST_INT_1;
				}
				
				switch(currentProperty){
					case TEST_INT_1 :{
						testInt1 |= (nextByte << (testIntIdx * 8)); // shift by 8 bits for each byte
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
		}

		@Override
		public TestEvent buildEventAndResetBuffer() throws UnMarshalableException {
			
			String testString1 = this.testString1.toString();
			int testInt1 = this.testInt1;
			
			this.testInt1 = 0;
			this.testIntIdx = 0;
			this.testString1.setLength(0); // reset string buffer
			this.lastRead = 0;
			return new TestEvent(testString1.toString(), testInt1);
		}
	}
	
	private static class TestDispatcher extends Dispatcher<TestEvent>{
		
		private volatile Collection<TestEvent> events = Collections.synchronizedCollection(new ArrayList<TestEvent>());
		private CountDownLatch latch;
		
		private TestDispatcher(int numberEventsExpected){
			this.latch = new CountDownLatch(numberEventsExpected);
		}
		
		@Override
		public void dispatchEvent(TestEvent event) {
			this.events.add(event);
			
			latch.countDown();

		}

		public void waitForEvent(int numEvents) throws InterruptedException {
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
		
	}
}
