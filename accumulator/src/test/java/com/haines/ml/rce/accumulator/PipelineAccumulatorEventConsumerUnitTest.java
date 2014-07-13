package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.system.Clock.StaticClock;

public class PipelineAccumulatorEventConsumerUnitTest {

	private static final long INITIAL_STARTUP_TIME = System.currentTimeMillis();
	//private static final TestConfig TEST_CONFIG = new TestConfig();
	
	private PipelineAccumulatorController<TestAccumulatorLookupStrategy> candidate;
	private StaticClock clock;
	
	public void before(){
		
		clock = new StaticClock(INITIAL_STARTUP_TIME);
		
		//candidate = new PipelineAccumulatorEventConsumer<TestEvent>(new AccumulatorEventConsumer<TestEvent>(TEST_CONFIG, new TestAccumulatorLookupStrategy()), nextStageConsumer, clock, TEST_CONFIG);
		candidate.systemStarted();
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
		public int[] getSlots(TestEvent event) {
			return event.getSlots();
		}

		@Override
		public int getMaxIndex() {
			return 64 * 64 * 4096;
		}

		@Override
		public void clear() {
			// NoOp
		}
	}
}
