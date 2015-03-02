package com.haines.ml.rce.main.factory;

import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.model.ClassifiedEvent;

public interface FeatureHandlerRepositoryFactory {

	FeatureHandlerRepositoryFactory ALL_DISCRETE_FEATURES = new FeatureHandlerRepositoryFactory() {

		@Override
		public <E extends ClassifiedEvent> HandlerRepository<E> create() {
			return HandlerRepository.create();
		}
	};

	<E extends ClassifiedEvent> HandlerRepository<E> create();
}
