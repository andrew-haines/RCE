package com.haines.ml.rce.main.factory;

import com.haines.ml.rce.main.RCEApplication;

public interface RCEApplicationFactory {

	RCEApplication createApplication(String configOverrideLocation);
}
