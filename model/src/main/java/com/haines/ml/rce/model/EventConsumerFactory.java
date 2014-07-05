package com.haines.ml.rce.model;

public interface EventConsumerFactory<E extends Event, EC extends EventConsumer<E>> {

	EC create();
}
