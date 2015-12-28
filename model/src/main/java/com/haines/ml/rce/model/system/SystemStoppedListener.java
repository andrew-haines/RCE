package com.haines.ml.rce.model.system;

/**
 * A system listener that notifies implementations of system shutdown events
 * @author haines
 *
 */
public interface SystemStoppedListener extends SystemListener {

	/**
	 * Signals to implementing classes when the system has successfully stopped all threads, and io paths and cleared down
	 * data structures.
	 */
	void systemStopped();
}
