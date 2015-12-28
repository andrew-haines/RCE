package com.haines.ml.rce.model;

import java.util.Collection;

/**
 * An event that is also attributed to a list of classifications
 * @author haines
 *
 */
public interface ClassifiedEvent extends FeaturedEvent{

	/**
	 * Returns the classifications of this event.
	 * @return
	 */
	Collection<? extends Classification> getClassificationsList();
}
