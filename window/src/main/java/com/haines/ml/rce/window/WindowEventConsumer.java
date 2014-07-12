package com.haines.ml.rce.window;

import javax.inject.Inject;

import com.haines.ml.rce.accumulator.AccumulatorProvider;
import com.haines.ml.rce.accumulator.lookups.RONaiveBayesMapBasedLookupStrategy;
import com.haines.ml.rce.accumulator.model.AccumulatedEvent;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.naivebayes.NaiveBayesAccumulatorBackedCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;

public class WindowEventConsumer implements EventConsumer<AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy>>{

	private final WindowManager aggregator;
	
	@Inject
	public WindowEventConsumer(WindowManager aggregator){
		this.aggregator = aggregator;
	}
	
	@Override
	public void consume(AccumulatedEvent<RONaiveBayesMapBasedLookupStrategy> event) {
		
		AccumulatorProvider accumulatorProvider = event.getAccumulatorProvider();
		
		NaiveBayesCountsProvider countsProvider = new NaiveBayesAccumulatorBackedCountsProvider(accumulatorProvider, event.getLookupStrategy().getIndexes());
		
		// need to ensure that this call uses the single writer paradigm
		aggregator.addNewProvider(countsProvider);
	}

}
