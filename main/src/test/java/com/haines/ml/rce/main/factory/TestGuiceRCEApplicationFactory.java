package com.haines.ml.rce.main.factory;

import java.nio.channels.ServerSocketChannel;

import com.google.inject.Module;
import com.google.inject.Provider;
import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.guice.AsyncPipelineRCEConfigConfiguredInitiationModule;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.test.model.TestEvent;

public class TestGuiceRCEApplicationFactory extends GuiceRCEApplicationFactory{

	public TestGuiceRCEApplicationFactory() {
		super(null);
	}

	@Override
	protected Module getInitialisationModule(String configOverrideLocation) {
		return new AsyncPipelineRCEConfigConfiguredInitiationModule<ServerSocketChannel, TestEvent>(configOverrideLocation, TestEvent.class, ServerSocketChannel.class, new Provider<EventMarshalBuffer<TestEvent>>(){

			@Override
			public EventMarshalBuffer<TestEvent> get() {
				return new ProtostuffEventMarshalBuffer<>(TestEvent.getSchema());
			}
			
		});
	}
}
