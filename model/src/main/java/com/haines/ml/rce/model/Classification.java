package com.haines.ml.rce.model;

public interface Classification {

	public static final Classification UNKNOWN = new Classification(){

		@Override
		public String getValue() {
			return "Unknown";
		}
		
	};
	
	String getValue();
}
