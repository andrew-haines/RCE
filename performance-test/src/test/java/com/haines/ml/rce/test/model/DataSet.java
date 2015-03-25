package com.haines.ml.rce.test.model;

import java.util.List;

import com.haines.ml.rce.model.Classification;

public interface DataSet {

	List<? extends Classification> getExpectedClasses();
}
