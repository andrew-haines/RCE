package com.haines.ml.rce.main.factory;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.test.model.TestEvent;
import com.haines.ml.rce.window.WindowManager;

public class TestNaiveBayesRCEApplicationFactory implements RCEApplicationFactory{
	
	private Iterable<SystemListener> startupListeners = null;

	@Override
	public RCEApplication createApplication(String configOverrideLocation) {
		
		try {
			RCEConfig config = RCEApplicationFactory.UTIL.loadConfig(configOverrideLocation);
			Clock clock = Clock.SYSTEM_CLOCK;
			
			WindowManager manager = new WindowManager(RCEConfig.UTIL.getWindowConfig(config), clock);
			
			RCEApplicationFactory factory =  NaiveBayesRCEApplicationFactory.getSyncNaiveBayesRCEApplicationFactory(new ProtostuffEventMarshalBuffer<TestEvent>(TestEvent.getSchema()), config, manager, clock);
			
			if (startupListeners != null){
				factory.addSystemListeners(startupListeners);
			}
			
			return factory.createApplication(configOverrideLocation);
		} catch (JAXBException | IOException e) {
			throw new RuntimeException("Unable to create test naive bayes RCE application factory", e);
		}
	}

	@Override
	public void addSystemListeners(Iterable<SystemListener> startupListeners) {
		this.startupListeners = startupListeners;
	}

}
