package com.haines.ml.rce.window;

import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;

public interface WindowUpdatedListener extends SystemListener{

	public static final WindowUpdatedListener NO_OP_LISTENER = new WindowUpdatedListener() {
		
		@Override
		public void newWindowCreated(NaiveBayesProbabilitiesProvider window) {
			// NoOp
		}
	};

	void newWindowCreated(NaiveBayesProbabilitiesProvider window);
}
