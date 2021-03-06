package com.haines.ml.rce.main.factory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBException;

import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.system.SystemListener;

public interface RCEApplicationFactory<E extends Event> {
	
	public static final Util UTIL = new Util();

	RCEApplication<E> createApplication(String configOverrideLocation);
	
	public static class Util{
		
		private Util(){}
		
		public RCEConfig loadConfig(String overrideLocation) throws JAXBException, IOException {
			Path overrideLocationPath = null;
			if (overrideLocation != null){
				overrideLocationPath = Paths.get(overrideLocation);
			}
			
			return RCEConfig.UTIL.loadConfig(overrideLocationPath);
		}
	}

	void addSystemListeners(Iterable<? extends SystemListener> startupListeners);
	
	void useSpecificConfig(RCEConfig config);
	
	void useSpecificHandlerRepository(FeatureHandlerRepositoryFactory featureHandlerRepo); 
}
