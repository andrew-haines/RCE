package com.haines.ml.rce.accumulator;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class NaiveBayesAccumulator<T extends Event> implements EventConsumer<T>{

	private final AccumulatorEventConsumer<T> featureJointAccumulatorConsumer;
	private final AccumulatorEventConsumer<T> classificationAccumulatorConsumer;
	
	public NaiveBayesAccumulator(AccumulatorEventConsumer<T> featureJointAccumulatorConsumer, AccumulatorEventConsumer<T> classificationAccumulatorConsumer){
		this.featureJointAccumulatorConsumer = featureJointAccumulatorConsumer;
		this.classificationAccumulatorConsumer = classificationAccumulatorConsumer;
	}
	
	@Override
	public void consume(T event) {
		featureJointAccumulatorConsumer.consume(event);
		classificationAccumulatorConsumer.consume(event);
	}

}
