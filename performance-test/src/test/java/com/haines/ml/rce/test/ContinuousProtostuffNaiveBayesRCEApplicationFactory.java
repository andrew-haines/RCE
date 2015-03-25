package com.haines.ml.rce.test;

import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.main.factory.ProtostuffNaiveBayesRCEApplicationFactory;
import com.haines.ml.rce.test.model.ContinuousTestEvent;

public class ContinuousProtostuffNaiveBayesRCEApplicationFactory extends ProtostuffNaiveBayesRCEApplicationFactory<ContinuousTestEvent>{

	public ContinuousProtostuffNaiveBayesRCEApplicationFactory() {
		super(ContinuousTestEvent.SCHEMA, Mode.SYNC);
	}
}