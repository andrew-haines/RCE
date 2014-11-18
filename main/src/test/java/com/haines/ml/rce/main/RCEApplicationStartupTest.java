package com.haines.ml.rce.main;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

import com.haines.ml.rce.eventstream.EventStreamListener;

public class RCEApplicationStartupTest {

	private RCEApplication candidate;
	private CountDownLatch started;
	private CountDownLatch finished;
	
	@Before
	public void before() throws RCEApplicationException{
		
		started = new CountDownLatch(1);
		finished = new CountDownLatch(1);
		
		candidate = new RCEApplication.RCEApplicationBuilder(null).addSystemStartedListener(new EventStreamListener() {

			@Override
			public void streamStarted() {
				started.countDown();
			}

			@Override
			public void streamStopped() {
				finished.countDown();
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
		
		// now stop the system
		
		candidate.stop();
		
		finished.await();
	}
}
