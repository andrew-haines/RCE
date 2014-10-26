package com.haines.ml.rce.main.factory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.haines.ml.rce.main.RCEApplication;

public class GuiceRCEApplicationFactory implements RCEApplicationFactory{
	
	private final Module initiationModule;
	
	public GuiceRCEApplicationFactory(Module initiationModule){
		this.initiationModule = initiationModule;
	}
	
	protected Module getInitialisationModule(String configOverrideLocation){
		return initiationModule;
	}

	@Override
	public RCEApplication createApplication(String configOverrideLocation) {
		Injector injector = Guice.createInjector(getInitialisationModule(configOverrideLocation));
		
		return injector.getInstance(RCEApplication.class);
	}
}
