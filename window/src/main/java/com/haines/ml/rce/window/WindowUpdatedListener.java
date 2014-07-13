package com.haines.ml.rce.window;

public interface WindowUpdatedListener {

	public static final WindowUpdatedListener NO_OP_LISTENER = new WindowUpdatedListener() {
		
		@Override
		public void windowUpdated(WindowManager window) {
			// NoOp
		}
	};

	void windowUpdated(WindowManager window);
}
