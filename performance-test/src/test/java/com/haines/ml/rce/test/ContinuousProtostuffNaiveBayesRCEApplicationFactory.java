package com.haines.ml.rce.test;

import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.main.factory.ProtostuffNaiveBayesRCEApplicationFactory;
import com.haines.ml.rce.test.ContinuousPerformanceTest.ContiuousTestEvent;

public class ContinuousProtostuffNaiveBayesRCEApplicationFactory extends ProtostuffNaiveBayesRCEApplicationFactory<ContiuousTestEvent>{

	public ContinuousProtostuffNaiveBayesRCEApplicationFactory() {
		super(ContiuousTestEvent.SCHEMA, Mode.SYNC);
	}
}