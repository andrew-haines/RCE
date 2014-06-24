package com.haines.ml.rce.main.config;

public class OverrideRCEConfig implements RCEConfig{

	private final RCEConfig defaultConfig;
	private final RCEConfig overrideConfig;
	
	public OverrideRCEConfig(RCEConfig defaultConfig, RCEConfig overrideConfig){
		if (defaultConfig == null){
			throw new IllegalArgumentException("Unable to create config. Default cannot be null");
		}
		this.defaultConfig = defaultConfig;
		this.overrideConfig = overrideConfig;
	}
	
	@Override
	public Integer getNumberOfEventWorkers() {
		return doOverride(new ValueGetter<Integer>(){

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getNumberOfEventWorkers();
			}
		});
	}
	@Override
	public StreamType getEventTransportProtocal() {
		return doOverride(new ValueGetter<StreamType>(){

			@Override
			public StreamType getValue(RCEConfig config) {
				return config.getEventTransportProtocal();
			}
		});
	}
	
	private <T> T doOverride(ValueGetter<T> getter){
		T value = null;
		if (overrideConfig != null){
			value = getter.getValue(overrideConfig);
		}
		
		if (value == null){
			value = getter.getValue(defaultConfig);
		}
		
		return value;
	}
	
	private static interface ValueGetter<T>{
		
		T getValue(RCEConfig config);
	}
}
