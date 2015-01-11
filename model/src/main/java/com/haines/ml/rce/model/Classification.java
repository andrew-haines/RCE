package com.haines.ml.rce.model;

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
	
	Object getValue();
	
	int getType();
}
