package com.haines.ml.rce.window;

import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;

public interface WindowUpdatedListener extends SystemListener{

	public static final WindowUpdatedListener NO_OP_LISTENER = new WindowUpdatedListener() {
		
		@Override
		public void windowUpdated(NaiveBayesProbabilitiesProvider window) {
			// NoOp
		}
	};

	void windowUpdated(NaiveBayesProbabilitiesProvider window);
}
