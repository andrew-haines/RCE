package com.haines.ml.rce.main.factory;

import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.haines.ml.rce.main.guice.AsyncPipelineRCEConfigConfiguredInitiationModule;
import com.haines.ml.rce.model.EventMarshalBuffer;
import com.haines.ml.rce.model.UnMarshalableException;
import com.haines.ml.rce.test.TestEvent;

public class TestGuiceRCEApplicationFactory extends GuiceRCEApplicationFactory{

	public TestGuiceRCEApplicationFactory() {
		super(null);
	}

	@Override
	protected Module getInitialisationModule(String configOverrideLocation) {
		return new AsyncPipelineRCEConfigConfiguredInitiationModule<ServerSocketChannel, TestEvent>(configOverrideLocation, TestEvent.class, ServerSocketChannel.class, new Provider<EventMarshalBuffer<TestEvent>>(){

			@Override
			public EventMarshalBuffer<TestEvent> get() {
				return new TestEventMarshalBuffer();
			}
			
		});
	}
	
	private static class TestEventMarshalBuffer implements EventMarshalBuffer<TestEvent>{

		private com.haines.ml.rce.test.model.TestEvent event = new com.haines.ml.rce.test.model.TestEvent();
		
		@Override
		public boolean marshal(ByteBuffer content) {
			ProtostuffIOUtil.mergeFrom(content.array(), event, event.cachedSchema());
			return true;
		}

		@Override
		public TestEvent buildEventAndResetBuffer() throws UnMarshalableException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
