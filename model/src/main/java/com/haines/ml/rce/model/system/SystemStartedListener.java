package com.haines.ml.rce.model.system;

/**
 * A listener that implementations can subscribe to to get a notification that the system
 * has started and ready to process events.
 * @author haines
 *
 */
public interface SystemStartedListener extends SystemListener{

	void systemStarted();
}
