package com.haines.ml.rce.accumulator;

import org.junit.Before;

import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.system.Clock.StaticClock;

public class PipelineAccumulatorControllerUnitTest {

	private static final long INITIAL_STARTUP_TIME = System.currentTimeMillis();
	private static final PipelineAccumulatorConfig TEST_CONFIG = new PipelineAccumulatorConfig(){

		@Override
		public long getPushIntervalTimeMs() {
			return 100;
		}
		
	};
	
	private PipelineAccumulatorController candidate;
	private StaticClock clock;
	
	@Before
	public void before(){
		
		clock = new StaticClock(INITIAL_STARTUP_TIME);
		
		candidate = new PipelineAccumulatorController(clock, TEST_CONFIG);
	}
	
	private static class TestEvent implements Event{
		
		private final int[] slots;
		
		private TestEvent(int[] slots){
			this.slots = slots;
		}
		
		private int[] getSlots(){
			return slots;
		}
	}
	
	private static class TestAccumulatorLookupStrategy implements AccumulatorLookupStrategy<TestEvent>{

		@Override
		public int[] getSlots(Feature feature, TestEvent event) {
			return event.getSlots();
		}
		
		@Override
		public int getSlot(Classification classification, TestEvent event) {
			return -1;
		}

		@Override
		public int getMaxIndex() {
			return 64 * 64 * 4096;
		}

		@Override
		public void clear() {
			// NoOp
		}

		@Override
		public AccumulatorLookupStrategy<TestEvent> copy() {
			return null;
		}

		@Override
		public int[] getPosteriorSlots(Feature feature,Classification classification, int numSlots) {
			return null;
		}

		@Override
		public int[] getClassificationSlots(Classification classification,int numSlots) {
			return null;
		}

		@Override
		public int getClassificationSlot(Classification classification) {
			return 0;
		}
	}
}
