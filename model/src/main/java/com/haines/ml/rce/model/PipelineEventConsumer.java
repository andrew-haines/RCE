package com.haines.ml.rce.model;

public interface PipelineEventConsumer {

	/**
	 * Notification to the implementing consumer that this event pipeline has changed owners.
	 * Use this to enforce any memory barriers that might be appropriate
	 */
	void pipelineOwnershipUpdated();
}
