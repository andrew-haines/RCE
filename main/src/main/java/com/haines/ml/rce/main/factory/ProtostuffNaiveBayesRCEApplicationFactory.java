package com.haines.ml.rce.main.factory;

import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;
import com.haines.ml.rce.transport.Event;

public class ProtostuffNaiveBayesRCEApplicationFactory implements RCEApplicationFactory<Event>{
	
	private Iterable<SystemListener> startupListeners = null;
	private RCEConfig testConfig;

	@Override
	public NaiveBayesRCEApplication<Event> createApplication(String configOverrideLocation) {
		
		NaiveBayesRCEApplicationFactory<Event> factory = new NaiveBayesRCEApplicationFactory<>(new ProtostuffEventMarshalBuffer<Event>(Event.getSchema()), Mode.SYNC);
	
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