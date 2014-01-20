package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class SwitchableAccumulatorEventConsumer<E extends Event, T extends EventConsumer<E>> implements EventConsumer<E>{

	private final T[] consumers;
	private volatile byte liveConsumer;
	
	@SuppressWarnings("unchecked")
	public SwitchableAccumulatorEventConsumer(T consumer1, T consumer2){
		consumers = (T[])new EventConsumer[2];
		
		consumers[0] = consumer1;
		consumers[1] = consumer2;
		
		liveConsumer = 0;
	}
	
	@Override
	public void consume(E event) {
		consumers[liveConsumer].consume(event);
	}
	
	public T switchLiveConsumer(){
		
		int stagingConsumer = this.liveConsumer;
		this.liveConsumer = (byte)((this.liveConsumer+1) & 0x1);
		
		return consumers[stagingConsumer];
		
	}

}
