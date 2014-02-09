package com.haines.ml.rce.model;

import java.util.Collection;

public interface ClassifiedEvent extends Event{

	Collection<Classification> getClassifications();
}
