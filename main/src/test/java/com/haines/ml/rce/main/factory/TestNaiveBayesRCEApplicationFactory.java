package com.haines.ml.rce.main.factory;

import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;
import com.haines.ml.rce.test.model.TestEvent;

public class TestNaiveBayesRCEApplicationFactory implements RCEApplicationFactory<TestEvent>{
	
	private Iterable<SystemListener> startupListeners = null;
	private RCEConfig testConfig;

	@Override
	public NaiveBayesRCEApplication<TestEvent> createApplication(String configOverrideLocation) {
		
		NaiveBayesRCEApplicationFactory<TestEvent> factory = new NaiveBayesRCEApplicationFactory<>(new ProtostuffEventMarshalBuffer<TestEvent>(TestEvent.getSchema()), Mode.SYNC);
	
		factory.addSystemListeners(startupListeners);
		factory.useSpecificConfig(testConfig);
		
		return factory.createApplication(configOverrideLocation);
	}

	@Override
	public void addSystemListeners(Iterable<SystemListener> startupListeners) {
		this.startupListeners = startupListeners;
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		this.testConfig = config;
	}

}
