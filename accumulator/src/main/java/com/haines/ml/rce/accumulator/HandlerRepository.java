package com.haines.ml.rce.accumulator;

import java.util.Collections;
import java.util.Map;

import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.DiscreteLookupAccumulatorHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.FeaturedEvent;

/**
 * Provides an index repository that maps {@link FeatureHandler} and {@link ClassificationHandler} handlers to given 
 * feature and classification types ({@link Feature#getType()}/{@link Classification#getType()}).
 * @author haines
 *
 * @param <E>
 */
public class HandlerRepository<E extends Event> {
	
	private final Map<Integer, FeatureHandler<E>> featureHandlers;
	private final Map<Integer, ClassificationHandler<E>> classificationHandlers;
	private final FeatureHandler<E> defaultFeatureHandler;
	private final ClassificationHandler<E> defaultClassificationHandler;
	
	public HandlerRepository(Map<Integer, FeatureHandler<E>> featureHandlers, Map<Integer, ClassificationHandler<E>> classificationHandlers, FeatureHandler<E> defaultFeatureHandler, ClassificationHandler<E> defaultClassificationHandler){
		this.featureHandlers = featureHandlers;
		this.classificationHandlers = classificationHandlers;
		this.defaultFeatureHandler = defaultFeatureHandler;
		this.defaultClassificationHandler = defaultClassificationHandler;
	}
	
	public static <E extends FeaturedEvent> HandlerRepository<E> create(Map<Integer, FeatureHandler<E>> featureHandlers,  Map<Integer, ClassificationHandler<E>> classificationHandlers){
		DiscreteLookupAccumulatorHandler<E> lookupHandler = new DiscreteLookupAccumulatorHandler<E>();
		
		return new HandlerRepository<E>(featureHandlers, classificationHandlers, lookupHandler, lookupHandler);
	}
	
	public static <E extends FeaturedEvent> HandlerRepository<E> create(){
		return create(Collections.<Integer, FeatureHandler<E>>emptyMap(), Collections.<Integer, ClassificationHandler<E>>emptyMap());
	}
	
	/**
	 * Returns the handler for this feature
	 * @param feature
	 * @return
	 */
	public FeatureHandler<E> getFeatureHandler(Feature feature){
		return this.getFeatureHandler(feature.getType());
	}
	
	/**
	 * Returns the handler for this feature type
	 * @param feature
	 * @return
	 */
	public FeatureHandler<E> getFeatureHandler(int featureType){
		FeatureHandler<E> handler = featureHandlers.get(featureType);
		
		if (handler == null){
			handler = defaultFeatureHandler;
		}
		
		return handler;
	}
	
	/**
	 * Returns the handler for this classification
	 * @param feature
	 * @return
	 */
	public ClassificationHandler<E> getClassificationHandler(Classification classification){
		return getClassificationHandler(classification.getType());
	}
	
	/**
	 * Returns the handler for this classification type
	 * @param feature
	 * @return
	 */
	public ClassificationHandler<E> getClassificationHandler(int classificationType){
		ClassificationHandler<E> handler = classificationHandlers.get(classificationType);
		
		if (handler == null){
			handler = defaultClassificationHandler;
		}
		
		return handler;
	}
}