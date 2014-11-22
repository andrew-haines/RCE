package com.haines.ml.rce.main.config;

import java.net.SocketAddress;
import java.nio.ByteOrder;

import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;

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

	@Override
	public Integer getEventBufferCapacity() {
		return doOverride(new ValueGetter<Integer>(){

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getEventBufferCapacity();
			}
		});
	}

	@Override
	public BufferType getEventBufferType() {
		return doOverride(new ValueGetter<BufferType>(){

			@Override
			public BufferType getValue(RCEConfig config) {
				return config.getEventBufferType();
			}
		});
	}

	@Override
	public ByteOrder getByteOrder() {
		return doOverride(new ValueGetter<ByteOrder>(){

			@Override
			public ByteOrder getValue(RCEConfig config) {
				return config.getByteOrder();
			}
		});
	}

	@Override
	public SocketAddress getEventStreamSocketAddress() {
		return doOverride(new ValueGetter<SocketAddress>(){

			@Override
			public SocketAddress getValue(RCEConfig config) {
				return config.getEventStreamSocketAddress();
			}
		});
	}

	@Override
	public Integer getFirstAccumulatorLineBitDepth() {
		return doOverride(new ValueGetter<Integer>(){

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getFirstAccumulatorLineBitDepth();
			}
		});
	}

	@Override
	public Integer getSecondAccumulatorLineBitDepth() {
		return doOverride(new ValueGetter<Integer>(){

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getSecondAccumulatorLineBitDepth();
			}
		});
	}

	@Override
	public Integer getFinalAccumulatorLineBitDepth() {
		return doOverride(new ValueGetter<Integer>(){

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getSecondAccumulatorLineBitDepth();
			}
		});
	}
	
	private static interface ValueGetter<T>{
		
		T getValue(RCEConfig config);
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

	@Override
	public int getDisruptorRingSize() {
		return doOverride(new ValueGetter<Integer>() {

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getDisruptorRingSize();
			}
		});
	}

	@Override
	public long getMicorBatchIntervalMs() {
		return doOverride(new ValueGetter<Long>(){

			@Override
			public Long getValue(RCEConfig config) {
				return config.getMicorBatchIntervalMs();
			}
			
		});
	}

	@Override
	public int getNumWindows() {
		return doOverride(new ValueGetter<Integer>(){

			@Override
			public Integer getValue(RCEConfig config) {
				return config.getNumWindows();
			}
		});
	}

	@Override
	public long getWindowPeriod() {
		return doOverride(new ValueGetter<Long>(){

			@Override
			public Long getValue(RCEConfig config) {
				return config.getWindowPeriod();
			}
		});
	}
}
