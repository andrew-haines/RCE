package com.haines.ml.rce.model;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * A switchable accumulator consumer that defines 2 pipelines - live and staging - that ensentially
 * allow the single writer paradim to be transfered to a different thread. By switching consumers
 * using the {@link #switchLiveConsumer()} method you are transferring ownership for writes to
 * the thread calling that method. When this happens, no other thread will be able to read or write
 * to the returned consumer. This class also guarantees that any thread that was writing to the now
 * staging consumer has finished by the time that this method returns. Its worth noting, that due to
 * the actual switching occurring on the consumer thread so that memory barriers can be put in place,
 * an additional consumer event will need to be triggered before the barrier can be put in place
 * and the coordinator thread 
 * @author haines
 *
 * @param <E>
 * @param <T>
 */
public class PipelinedEventConsumer<E extends Event, T extends EventConsumer<E>> implements EventConsumer<E>{

	private final static byte LIVE_CONSUMER_MASK = 0x1;
	private final static byte LIVE_CONSUMER_ACTIVE_MASK = 0x2;
	private final static byte STAGING_CONSUMER_ACTIVE_MASK = 0x4;
	private final static byte ATTEMPTING_TO_SWITCH = 0x8;
	private final Condition ownershipTransferNotifier = new AbstractQueuedSynchronizer(){}.new ConditionObject();
	
	private final T[] consumers;
	private volatile byte liveConsumerState;
	
	@SuppressWarnings("unchecked")
	public PipelinedEventConsumer(T consumer1, T consumer2){
		consumers = (T[])new EventConsumer[2];
		
		consumers[0] = consumer1;
		consumers[1] = consumer2;
		
		liveConsumerState = 0;
	}
	
	@Override
	public void consume(E event) {
		
		byte liveConsumerState = this.liveConsumerState; // copies to local variable obtaining atomisity
		int liveConsumer = getLiveConsumer(liveConsumerState);
		
		/*
		 *  set active consumer to be active. marks that there is a consumer thread 
		 *  currently working in the live consumer. Note the shift by what is considered a
		 *  live consumer so that the relevant active bit for the 'live' consumer is set
		 */
		
		int activeConsumerBitMask = (LIVE_CONSUMER_ACTIVE_MASK << liveConsumer);
		this.liveConsumerState |= activeConsumerBitMask; //write to main memory
			try{
				consumers[liveConsumer].consume(event);
		} finally{
			
			this.liveConsumerState ^= activeConsumerBitMask; // unset active consumer
			
		}
	}
	
	private final int getLiveConsumer(byte consumerBitSet) {
		return consumerBitSet & LIVE_CONSUMER_MASK;
	}

	public T switchLiveConsumer() throws InterruptedException{
		
		byte stagingConsumer = this.liveConsumerState;
		int toBeStagingConsumer = getLiveConsumer(stagingConsumer);
		
		// first switch consumers so that live->staging and staging->live
		
		this.liveConsumerState ^= LIVE_CONSUMER_MASK; // flip the live consumer bit.
		
		// now check to see if we have a consumer currently processing the old live consumer
		
		int liveConsumerActiveMask = LIVE_CONSUMER_ACTIVE_MASK << toBeStagingConsumer;
		
		while ((liveConsumerState & liveConsumerActiveMask) == liveConsumerActiveMask){
			// busy spin.
		}
		
		return consumers[toBeStagingConsumer];
		
	}

}
