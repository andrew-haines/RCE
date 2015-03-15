package com.haines.ml.rce.main.factory;

import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.transport.Event;

public class StaticProtostuffNaiveBayesRCEApplicationFactory extends ProtostuffNaiveBayesRCEApplicationFactory<Event>{

	public StaticProtostuffNaiveBayesRCEApplicationFactory() {
		super(Event.getSchema(), Mode.SYNC);
	}

}
