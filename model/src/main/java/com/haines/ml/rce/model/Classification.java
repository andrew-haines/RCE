package com.haines.ml.rce.model;

/**
 * An interface that determines a classification value that can be assigned to a given event.
 * @author haines
 *
 */
public interface Classification {

	public static final Classification UNKNOWN = new Classification(){

		@Override
		public Object getValue() {
			return "Unknown";
		}
		
		@Override
		public String toString(){
			return getValue().toString();
		}

		@Override
		public int getType() {
			return -1;
		}
		
	};
	
	/**
	 * Returns the classification value
	 * @return
	 */
	Object getValue();
	
	/**
	 * Returns a numerical type for this classification. Allows support for multiple classification values to be
	 * assigned to an event.
	 * @return
	 */
	int getType();
}
