package com.haines.ml.rce.main.factory;

import java.nio.channels.ServerSocketChannel;

import com.google.inject.Module;
import com.google.inject.Provider;
import com.haines.ml.rce.io.protostuff.ProtostuffEventMarshalBuffer;
import com.haines.ml.rce.main.guice.AsyncPipelineRCEConfigConfiguredInitiationModule;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.transport.Event;

public class TestGuiceRCEApplicationFactory extends GuiceRCEApplicationFactory<Event>{

	public TestGuiceRCEApplicationFactory() {
		super(null);
	}

	@Override
	protected Module getInitialisationModule(String configOverrideLocation) {
		return new AsyncPipelineRCEConfigConfiguredInitiationModule<ServerSocketChannel, Event>(configOverrideLocation, Event.class, ServerSocketChannel.class, new Provider<EventMarshalBuffer<Event>>(){

			@Override
			public EventMarshalBuffer<Event> get() {
				return new ProtostuffEventMarshalBuffer<>(Event.getSchema());
			}
			
		});
	}
}
