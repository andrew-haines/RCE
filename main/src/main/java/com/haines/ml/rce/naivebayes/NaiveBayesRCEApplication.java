package com.haines.ml.rce.naivebayes;

import com.haines.ml.rce.main.RCEApplication;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

public class NaiveBayesRCEApplication<E extends Event> implements RCEApplication<E>{

	private final NaiveBayesService naiveBayesService;
	private final RCEApplication<E> rceApplication;
	
	public NaiveBayesRCEApplication(RCEApplication<E> rceApplication, NaiveBayesService naiveBayesService) {
		
		this.naiveBayesService = naiveBayesService;
		this.rceApplication = rceApplication;
	}
	
	public NaiveBayesService getNaiveBayesService(){
		return naiveBayesService;
	}

	@Override
	public void start() throws RCEApplicationException {
		rceApplication.start();
	}

	@Override
	public void stop() throws RCEApplicationException {
		rceApplication.stop();
	}

	@Override
	public EventConsumer<E> getEventConsumer() {
		return rceApplication.getEventConsumer();
	}

	@Override
	public RCEConfig getConfig() {
		return rceApplication.getConfig();
	}
}
