package com.haines.ml.rce.test.load;

import org.picocontainer.Startable;

import net.grinder.console.communication.ProcessControl;

public class GrinderMessagingInspector implements Startable{
	
	private static volatile GrinderMessagingInspector INSTANCE;

	private final ProcessControl processControl;
	
	public GrinderMessagingInspector(ProcessControl processControl){
		
		if (INSTANCE != null){
			throw new IllegalStateException("Unable to create instance as there is already one in the JVM");
		}
		this.processControl = processControl;
		
		
	}

	@Override
	public void start() {
		
		if (INSTANCE != null){
			throw new IllegalStateException("Unable to create instance as there is already one in the JVM");
		}
		
		INSTANCE = this;
	}

	@Override
	public void stop() {
		
		INSTANCE = null;
	}
	
	public static GrinderMessagingInspector getInstance(){
		if (INSTANCE == null){
			throw new IllegalStateException("JVM does not already have an instance of the inspector");
		}
		
		return INSTANCE;
	}

	public ProcessControl getProcessControl() {
		return processControl;
	}
}
