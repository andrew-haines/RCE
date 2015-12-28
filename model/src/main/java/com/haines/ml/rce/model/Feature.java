package com.haines.ml.rce.model;

/**
 * An abstraction to represent a feature of an event as a type and value. The type/value distinction is to allow a more natural
 * transition to non-homogenious serialisation mechanisms and to eliminate the need for a new class definition for each feature type.
 * @author haines
 *
 */
public interface Feature {

	/**
	 * Returns the type of this feature.
	 * @return
	 */
	int getType();
	
	/**
	 * Returns the actual value of this feature.
	 * @return
	 */
	Object getValue();
}
