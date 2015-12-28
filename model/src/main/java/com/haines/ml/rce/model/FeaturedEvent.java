package com.haines.ml.rce.model;

import java.util.Collection;

/**
 * An Event that is defined by a collection of features.
 * @author haines
 *
 */
public interface FeaturedEvent extends Event{

	/**
	 * Return a collection of features that represent this event
	 * @return
	 */
	Collection<? extends Feature> getFeaturesList();
}
