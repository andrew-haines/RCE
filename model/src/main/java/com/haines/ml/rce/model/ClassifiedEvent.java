package com.haines.ml.rce.model;

import java.util.Collection;

public interface ClassifiedEvent extends FeaturedEvent{

	Collection<Classification> getClassifications();
}
