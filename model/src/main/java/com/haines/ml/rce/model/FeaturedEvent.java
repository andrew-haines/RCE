package com.haines.ml.rce.model;

import java.util.Collection;

public interface FeaturedEvent extends Event{

	Collection<? extends Feature> getFeaturesList();
}
