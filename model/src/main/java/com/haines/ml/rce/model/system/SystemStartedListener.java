package com.haines.ml.rce.model.system;

/**
 * A listener that implementations can subscribe to to get a notification that the system
 * has started and ready to process events.
 * @author haines
 *
 */
public interface SystemStartedListener extends SystemListener{

	/**
	 * Notifies when the system has finished starting up all threads, io paths and data structures
	 */
	void systemStarted();
}
