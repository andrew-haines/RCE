package com.haines.ml.rce.eventstream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.dispatcher.Dispatcher;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventBuffer;
import com.haines.ml.rce.model.UnMarshalableException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

public abstract class AbstractSelectorEventStreamIT<T extends SelectableChannel & NetworkChannel> {

	private static final int TEST_PORT = 45322;
	private SelectorEventStream<T> candidate;
	private final Executor executor = Executors.newSingleThreadExecutor();
	private Dispatcher<TestEvent> dispatcher;
	private CountDownLatch startupLatch;
	
	@Before
	public void before() throws UnknownHostException{
		
		dispatcher = new TestDispatcher();
		
		SelectorEventStreamConfig config = new SelectorEventStreamConfig.SelectorEventStreamConfigBuilder()
		.bufferCapacity(2)
		.socketAddress(new InetSocketAddress(InetAddress.getLocalHost(), TEST_PORT))
		.build();
		
		startupLatch = new CountDownLatch(1);
		
		SelectorEventStreamFactory<T> streamFactory = new SelectorEventStreamFactory<T>(config, createNetworkChannelProcessor(), new TestEventBuffer(), new LatchNotifierEventStreamListener(startupLatch));
		candidate = streamFactory.create(dispatcher);
	}
	
	protected abstract NetworkChannelProcessor<T> createNetworkChannelProcessor();
	
	@Test
	public void givenCandidate_whenCallingStart_thenStreamIsAlive() throws EventStreamException, InterruptedException{
		assertThat(candidate.isAlive(), is(equalTo(false)));
		
		executor.execute(getStarter(candidate));
		
		startupLatch.await();
		assertThat(candidate.isAlive(), is(equalTo(true)));
	}
	
	private static Runnable getStarter(final SelectorEventStream<?> candidate){
		return new Runnable(){

			@Override
			public void run() {
				try {
					candidate.start();
				} catch (EventStreamException e) {
					throw new RuntimeException("Unable to start stream");
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
						testInt1 |= (nextByte << testIntIdx++);
					}
					case TEST_STRING1:{
						testString1.append((char)nextByte);
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
		
		
	}
	
	private static class LatchNotifierEventStreamListener implements EventStreamListener{

		private final CountDownLatch startupLatch;
		
		private LatchNotifierEventStreamListener(CountDownLatch startupLatch){
			this.startupLatch = startupLatch;
		}
		
		@Override
		public void streamStarted() {
			getStartupLatch().countDown();
		}

		@Override
		public void streamStopped() {

		}

		public CountDownLatch getStartupLatch() {
			return startupLatch;
		}
		
	}
}
