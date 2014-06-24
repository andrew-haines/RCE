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

	@Override
	public RCEApplication createApplication() {
		Injector injector = Guice.createInjector(initiationModule);
		
		return injector.getInstance(RCEApplication.class);
	}

}
