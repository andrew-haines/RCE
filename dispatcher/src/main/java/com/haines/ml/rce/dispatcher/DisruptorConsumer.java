package com.haines.ml.rce.dispatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.TimeoutException;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

public class DisruptorConsumer<T extends Event> implements DispatcherConsumer<T>{

	private final Disruptor<DisruptorEvent<T>> queue;
	private final EventTranslatorOneArg<DisruptorEvent<T>, T> translator;
	
	private DisruptorConsumer(Disruptor<DisruptorEvent<T>> queue){
		this.queue = queue;
		this.translator = new DisruptorConsumerTranslator<T>();
	}
	
	@Override
	public void consumeEvent(T event) {
		
		queue.publishEvent(translator, event);
	}
	
	public static class Builder<T extends Event>{
		
		private final ExecutorService executor;
		private final DisruptorConfig config;
		private final Collection<EventConsumer<T>> consumers;
		
		public Builder(ExecutorService executor, DisruptorConfig config){
			this.executor = executor;
			this.config = config;
			this.consumers = new ArrayList<EventConsumer<T>>();
		}
		
		public Builder<T> addConsumer(EventConsumer<T> consumer){
			
			consumers.add(consumer);
			
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public DisruptorConsumer<T> build(){
			Disruptor<DisruptorEvent<T>> queue = new Disruptor<DisruptorEvent<T>>(new DisruptorEventFactory<T>(), config.getRingSize(), executor, ProducerType.SINGLE, new SleepingWaitStrategy()){

				@Override
				public void shutdown(long timeout, TimeUnit timeUnit) throws TimeoutException { // overload so that disruptor will shutdown the executor
					super.shutdown(timeout, timeUnit);
					
					executor.shutdownNow();
				}
				
			};
			
			List<EventHandler<DisruptorEvent<T>>> eventHandlers = Lists.newArrayList(Iterables.transform(consumers, new Function<EventConsumer<T>, EventHandler<DisruptorEvent<T>>>(){

				@Override
				public EventHandler<DisruptorEvent<T>> apply(final EventConsumer<T> input) {
					return new EventHandler<DisruptorEvent<T>>(){

						@Override
						public void onEvent(DisruptorEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
							input.consume(event.getEvent());
						}
						
					};
				}
			}
			));
			
			queue.handleEventsWith(eventHandlers.toArray((EventHandler<DisruptorEvent<T>>[])new EventHandler[consumers.size()]));

			queue.start();
			return new DisruptorConsumer<T>(queue);
		}
	}

	private static class DisruptorEvent<T extends Event>{
		
		private T event;

		public T getEvent() {
			return event;
		}

		public void setEvent(T event) {
			this.event = event;
		}
	}
	
	private static class DisruptorEventFactory<E extends Event> implements EventFactory<DisruptorEvent<E>>{

		private DisruptorEventFactory(){}
		
		@Override
		public DisruptorEvent<E> newInstance() {
			return new DisruptorEvent<E>();
		}
	}
	
	private static class DisruptorConsumerTranslator<T extends Event> implements EventTranslatorOneArg<DisruptorEvent<T>, T>{

		@Override
		public void translateTo(DisruptorEvent<T> eventHolder, long sequence, T event) {
			eventHolder.setEvent(event);
		}
		
	}

	@Override
	public void shutdown() {
		queue.shutdown();
	}
}
