package com.haines.ml.rce.main.factory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.system.SystemListener;

public class GuiceRCEApplicationFactory<E extends Event> implements RCEApplicationFactory<E>{
	
	private final Module initiationModule;
	
	public GuiceRCEApplicationFactory(Module initiationModule){
		this.initiationModule = initiationModule;
	}
	
	protected Module getInitialisationModule(String configOverrideLocation){
		return initiationModule;
	}

	@Override
	public RCEApplication<E> createApplication(String configOverrideLocation) {
		Injector injector = Guice.createInjector(getInitialisationModule(configOverrideLocation));
		
		return injector.getInstance(RCEApplication.class);
	}

	@Override
	public void addSystemListeners(Iterable<SystemListener> startupListeners) {
		// NOOP
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void useSpecificHandlerRepository(
			FeatureHandlerRepositoryFactory featureHandlerRepo) {
		// TODO Auto-generated method stub
		
	}
}
