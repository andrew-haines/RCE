package com.haines.ml.rce.main.protostuff;

import com.haines.ml.rce.transport.ValueType;

public abstract class ProtostuffValueAdaptor {

	public abstract ValueType getValueType();
	
	public abstract long getLongValue();
	
	public abstract int getIntValue();
	
	public abstract float getFloatValue();
	
	public abstract double getDoubleValue();
	
	public abstract String getStringValue();
	
	public abstract int getType();
	
	public Object getValue() {
		switch (getValueType()){
		case DOUBLE:
			return getDoubleValue();
		case FLOAT:
			return getFloatValue();
		case INT:
			return getIntValue();
		case LONG:
			return getLongValue();
		case STRING:
			return getStringValue();
		default:
			throw new IllegalStateException("Unknown value type: "+getValueType());
		}
	}

	@Override
	public int hashCode(){
		int hash = 13;
		
		hash ^= getType();
		hash ^= getValueType().number;
		hash ^= getValue().hashCode();
		
		return hash;
	}
	
	@Override
	public boolean equals(Object other){
		ProtostuffValueAdaptor otherAdaptor = (ProtostuffValueAdaptor)other;
		
		return otherAdaptor.getType() == this.getType() &&
				otherAdaptor.getValueType() == this.getValueType() &&
				otherAdaptor.getValue().equals(this.getValue());
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder("{");
		
		builder.append("type=").append(getType()).append(";");
		builder.append("value=").append(getValue()).append(";");
		
		builder.append("}");
		return builder.toString();
	}
}
