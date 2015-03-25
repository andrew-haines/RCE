package com.haines.ml.rce.test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class DynamicClassLoader extends ClassLoader {

	DynamicClassLoader(){
		super(Thread.currentThread().getContextClassLoader());
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		
		if ("META-INF/services/com.haines.ml.rce.main.factory.RCEApplicationFactory".equals(name)){ // override the factory service file to use the dynamic protostuff factory
			name = name+"_dynamic"; 
		}
		return super.getResources(name);
	}
}
