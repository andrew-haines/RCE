package com.haines.ml.rce.window;

public interface WindowConfig {

	/**
	 * Returns the milliseconds that each window represents
	 * @return
	 */
	long getWindowPeriod();
	
	/**
	 * Number of windows to hold events for
	 * @return
	 */
	int getNumWindows();
}
