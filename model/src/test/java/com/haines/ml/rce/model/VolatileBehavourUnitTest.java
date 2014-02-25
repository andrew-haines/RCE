package com.haines.ml.rce.model;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
public class VolatileBehavourUnitTest {

	private static class NonVolatileClass {
		
		private int value;
		private int[] values = new int[1024];
	}
	
	private static class SomeVolatileClass {
		
		private volatile int value;
		private int[] values = new int[1024];
	}
	
	@Test
	public void test1() throws InterruptedException{
		
		for (int i = 0; i < 10; i++){
			final NonVolatileClass shared = new NonVolatileClass();
			//final CountDownLatch finished = new CountDownLatch(1);
			Thread writer = new Thread(new Runnable(){
	
				@Override
				public void run() {
					
					shared.value = 45657564;
					
					for (int i = 0; i < shared.values.length; i++){
						shared.values[i] = i;
					}
					//finished.countDown();
				}
				
			});
			
			writer.start();
			
			Thread.sleep(5);
			
			//finished.await();
			
			assertThat(shared.value, is(equalTo(45657564)));
			
			for (int i2 = 0; i2 < shared.values.length; i2++){
				assertThat(shared.values[i2], is(equalTo(i2)));
			}
		}
	}
}
