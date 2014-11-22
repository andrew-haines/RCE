package com.haines.ml.rce.main.config.jaxb;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.haines.ml.rce.eventstream.SelectorEventStreamConfig.BufferType;
import com.haines.ml.rce.main.config.RCEConfig;

@XmlRootElement(name="config")
public class RCEConfigJAXB implements RCEConfig{

	private static final String BIG_BYTE_ORDER = "big";

	private static final String LITTLE_BYTE_ORDER = "little";

	private int numberOfEventWorkers;
	
	private TransportConfigJaxB transport;
	
	private Integer eventBufferCapacity;
	
	private BufferType eventBufferType;
	
	private String eventByteOrder;

	private AccumulatorConfigJaxB accumulatorConfig;
	
	private int disruptorRingSize;
	
	private long microBatchIntervalMs;
	
	private WindowConfigJaxB window;

	@Override
	@XmlElement
	public Integer getNumberOfEventWorkers() {
		return numberOfEventWorkers;
	}

	public void setNumberOfEventWorkers(int numberOfEventWorkers) {
		this.numberOfEventWorkers = numberOfEventWorkers;
	}

	@Override
	public StreamType getEventTransportProtocal() {
		return StreamType.valueOf(transport.getProtocol());
	}

	@Override
	@XmlElement
	public Integer getEventBufferCapacity() {
		return eventBufferCapacity;
	}

	@Override
	@XmlElement
	public BufferType getEventBufferType() {
		return eventBufferType;
	}
	
	@Override
	@XmlElement
	public int getDisruptorRingSize() {
		return disruptorRingSize;
	}

	@Override
	public ByteOrder getByteOrder() {
		if (getEventByteOrder().equalsIgnoreCase(BIG_BYTE_ORDER)){
			return ByteOrder.BIG_ENDIAN;
		} else if (getEventByteOrder().equalsIgnoreCase(LITTLE_BYTE_ORDER)){
			return ByteOrder.LITTLE_ENDIAN;
		} else{
			throw new RuntimeException("Unknown byte order type: "+getEventByteOrder());
		}
	}

	@Override
	public SocketAddress getEventStreamSocketAddress() {
		return new InetSocketAddress(getEventStreamHost(), getEventStreamPort());
	}

	@Override
	public Integer getFirstAccumulatorLineBitDepth() {
		return getAccumulatorConfig().getAccumulatorFirstLineBitDepth();
	}

	@Override
	public Integer getSecondAccumulatorLineBitDepth() {
		return getAccumulatorConfig().getAccumulatorSecondLineBitDepth();
	}

	@Override
	public Integer getFinalAccumulatorLineBitDepth() {
		return getAccumulatorConfig().getAccumulatorFinalLineBitDepth();
	}

	public void setEventBufferCapacity(Integer eventBufferCapacity) {
		this.eventBufferCapacity = eventBufferCapacity;
	}

	public void setEventBufferType(BufferType eventBufferType) {
		this.eventBufferType = eventBufferType;
	}

	@XmlElement
	public String getEventByteOrder() {
		return eventByteOrder;
	}

	public void setEventByteOrder(String eventByteOrder) {
		this.eventByteOrder = eventByteOrder;
	}

	public int getEventStreamPort() {
		return transport.getPort();
	}

	public String getEventStreamHost() {
		return transport.getHost();
	}
	
	public void setDisruptorRingSize(int disruptorRingSize){
		this.disruptorRingSize = disruptorRingSize;
	}

	@Override
	@XmlElement(name="microBatchIntervalMs")
	public long getMicroBatchIntervalMs() {
		return microBatchIntervalMs;
	}

	public void setMicroBatchIntervalMs(long asyncPushIntervalMs){
		this.microBatchIntervalMs = asyncPushIntervalMs;
	}

	@XmlElement(name="accumulator")
	public AccumulatorConfigJaxB getAccumulatorConfig() {
		return accumulatorConfig;
	}

	public void setAccumulatorConfig(AccumulatorConfigJaxB accumulator) {
		this.accumulatorConfig = accumulator;
	}

	@Override
	public int getNumWindows() {
		return getWindow().getNumWindows();
	}

	@Override
	public long getWindowPeriod() {
		return getWindow().getWindowSizeMs();
	}

	@XmlElement(name="window")
	public WindowConfigJaxB getWindow() {
		return window;
	}

	public void setWindow(WindowConfigJaxB window) {
		this.window = window;
	}

	@XmlElement
	public TransportConfigJaxB getTransport() {
		return transport;
	}

	public void setTransport(TransportConfigJaxB transport) {
		this.transport = transport;
	}
}
