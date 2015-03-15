package com.haines.ml.rce.main.factory;

import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Schema;
import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.AccumulatorRCEApplicationFactory.Mode;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesRCEApplication;

public abstract class ProtostuffNaiveBayesRCEApplicationFactory<E extends Message<E> & ClassifiedEvent> implements RCEApplicationFactory<E>{
	
	private Iterable<SystemListener> startupListeners = null;
	private RCEConfig rceConfig;
	private FeatureHandlerRepositoryFactory featureHandlerRepo;
	private final Schema<E> schema;
	private final Mode mode;
	
	protected ProtostuffNaiveBayesRCEApplicationFactory(Schema<E> schema, Mode mode){
		this.schema = schema;
		this.mode = mode;
	}

	@Override
	public NaiveBayesRCEApplication<E> createApplication(String configOverrideLocation) {
		
		NaiveBayesRCEApplicationFactory<E> factory = new NaiveBayesRCEApplicationFactory<E>(new ProtostuffEventMarshalBuffer<E>(schema), mode);
	
		factory.addSystemListeners(startupListeners);
		factory.useSpecificConfig(rceConfig);
		factory.useSpecificHandlerRepository(featureHandlerRepo);
		
		return factory.createApplication(configOverrideLocation);
	}

	@Override
	public void addSystemListeners(Iterable<SystemListener> startupListeners) {
		this.startupListeners = startupListeners;
	}

	@Override
	public void useSpecificConfig(RCEConfig config) {
		this.rceConfig = config;
	}

	@Override
	public void useSpecificHandlerRepository(FeatureHandlerRepositoryFactory featureHandlerRepo) {
		this.featureHandlerRepo = featureHandlerRepo;
	}
}
