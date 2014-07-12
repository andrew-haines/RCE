package com.haines.ml.rce.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A switchable accumulator consumer that defines 2 pipelines - live and staging - that ensentially
 * allow the single writer paradim to be transfered to a different thread. By switching consumers
 * using the {@link #switchLiveConsumer()} method you are transferring ownership for writes to
 * the thread calling that method. When this happens, no other thread will be able to read or write
 * to the returned consumer. This class also guarantees that any thread that was writing to the now
 * staging consumer has finished by the time that this method returns. 
 * 
 * This class is meant to be used with at most 2 threads. One being the consumer thread, the thread that
 * calls the {@link #consume(Event)} method and the other the coordinator thread that calls the
 * {@link #switchLiveConsumer()}. 
 * 
 *                T(consumer)
 *                     \
 *                      \
 *                       \ w
 *                        \
 *                         \
 *        consumer0          consumer 1
 *            \
 *             \
 *              \ w
 *               \
 *                \
 *               T(coordinator)
 *               
 *               
 *               
 *        After coordinator calls {@link #switchLiveConsumer()}:
 *        
 *                T(consumer)
 *                     /
 *                    / 
 *                 w / 
 *                  / 
 *                 /
 *        consumer0          consumer 1
 *                              /
 *                             /
 *                            / w
 *                           /
 *                          /
 *               T(coordinator)
 *        
 *        
 *        
 *        
 * @author haines
 *
 * @param <E>
 * @param <T>
 */
public class PipelinedEventConsumer<E extends Event, T extends EventConsumer<E>> implements EventConsumer<E>{

	private final static Logger LOG = LoggerFactory.getLogger(PipelinedEventConsumer.class);
	private final static byte LIVE_CONSUMER_MASK = 0x1;
	private final static byte LIVE_CONSUMER_ACTIVE_MASK = 0x2;
	
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
		
		byte liveConsumerState = this.liveConsumerState; // copies to local variable obtaining atomicity
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

	public T switchLiveConsumer() {
		
		byte stagingConsumer = this.liveConsumerState;
		int toBeStagingConsumer = getLiveConsumer(stagingConsumer);
		
		LOG.debug("Switching live consumer: "+toBeStagingConsumer);
		
		// first switch consumers so that live->staging and staging->live
		
		this.liveConsumerState ^= LIVE_CONSUMER_MASK; // flip the live consumer bit.
		
		// now check to see if we have a consumer currently processing the old live consumer. We busy spin until no other thread is using this downstream consumer
		
		int liveConsumerActiveMask = LIVE_CONSUMER_ACTIVE_MASK << toBeStagingConsumer;
		
		while ((liveConsumerState & liveConsumerActiveMask) == liveConsumerActiveMask){
			// busy spin.
			Thread.yield();
		}
		
		LOG.debug("New Live Consumer: "+getLiveConsumer(liveConsumerState));
		
		return consumers[toBeStagingConsumer];
		
	}

}
