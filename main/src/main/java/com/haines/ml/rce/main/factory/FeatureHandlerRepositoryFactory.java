package com.haines.ml.rce.main.factory;

import com.haines.ml.rce.accumulator.FeatureHandlerRepository;
import com.haines.ml.rce.model.FeaturedEvent;

public interface FeatureHandlerRepositoryFactory {

	FeatureHandlerRepositoryFactory ALL_DISCRETE_FEATURES = new FeatureHandlerRepositoryFactory() {

		@Override
		public <E extends FeaturedEvent> FeatureHandlerRepository<E> create() {
			return FeatureHandlerRepository.create();
		}
	};

	<E extends FeaturedEvent> FeatureHandlerRepository<E> create();
}
