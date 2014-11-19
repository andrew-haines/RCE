package com.haines.ml.rce.main.factory;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;
import com.haines.ml.rce.naivebayes.NaiveBayesService;
import com.haines.ml.rce.test.model.TestEvent;
import com.haines.ml.rce.window.WindowManager;
import com.haines.ml.rce.window.WindowUpdatedListener;

public class TestNaiveBayesRCEApplicationFactory implements RCEApplicationFactory<TestEvent>{
	
	private Iterable<SystemListener> startupListeners = null;
	private RCEConfig testConfig;

	@Override
	public NaiveBayesRCEApplication<TestEvent> createApplication(String configOverrideLocation) {
		
		try {
			
			RCEConfig config = testConfig;
			
			if (config == null){
				config = RCEApplicationFactory.UTIL.loadConfig(configOverrideLocation);
			}
			Clock clock = Clock.SYSTEM_CLOCK;
			
			WindowManager manager = new WindowManager(RCEConfig.UTIL.getWindowConfig(config), clock, Iterables.filter(startupListeners, WindowUpdatedListener.class));
			
			NaiveBayesService classifierService = new NaiveBayesService(manager);
			
			RCEApplicationFactory<TestEvent> factory =  NaiveBayesRCEApplicationFactory.getSyncNaiveBayesRCEApplicationFactory(new ProtostuffEventMarshalBuffer<TestEvent>(TestEvent.getSchema()), config, manager, clock);
			
			if (startupListeners != null){
				factory.addSystemListeners(startupListeners);
			}
			
			factory.useSpecificConfig(config);
			
			return new NaiveBayesRCEApplication<TestEvent>(factory.createApplication(configOverrideLocation), classifierService);
		} catch (JAXBException | IOException e) {
			throw new RuntimeException("Unable to create test naive bayes RCE application factory", e);
		}
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
