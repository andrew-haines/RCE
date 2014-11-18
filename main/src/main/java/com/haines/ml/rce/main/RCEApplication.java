package com.haines.ml.rce.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.haines.ml.rce.eventstream.EventStream;
import com.haines.ml.rce.eventstream.EventStreamException;
import com.haines.ml.rce.main.factory.RCEApplicationFactory;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.model.system.SystemStartedListener;
public class RCEApplication {
	
	private final EventStream eventStream;
	
	@Inject
	public RCEApplication(EventStream eventStream){
		this.eventStream = eventStream;
	}

	public void start() throws RCEApplicationException {
		try {
			eventStream.start();
		} catch (EventStreamException e) {
			throw new RCEApplicationException("Unable to start RCE", e);
		}
	}
	
	public void stop() throws RCEApplicationException{
		try {
			eventStream.stop();
		} catch (EventStreamException e) {
			throw new RCEApplicationException("Unable to stop RCE", e);
		}
	}
	
	private static final String CONFIG_OVERRIDE_OPTION_KEY = "configOverrideLocation";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws ParseException, RCEApplicationException, JAXBException, IOException{
		Options options = new Options();
        options.addOption(OptionBuilder.hasArg(true).withDescription("Config Override Location").isRequired(false).create(CONFIG_OVERRIDE_OPTION_KEY));

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, args);

        RCEApplicationBuilder builder = new RCEApplicationBuilder(cmd.getOptionValue(CONFIG_OVERRIDE_OPTION_KEY));
        
        RCEApplication application = builder.build();
        
        application.start();
	}

	public static class RCEApplicationBuilder {
		
		// set the number of event worker to be 1 less than the number of CPUs on the VM
		private String configOverrideLocation;
		private Collection<SystemListener> startupListeners = new ArrayList<SystemListener>();
		
		public RCEApplicationBuilder(String configOverrideLocation){
			this.configOverrideLocation = configOverrideLocation;
		}
		
		public RCEApplicationBuilder addSystemStartedListener(SystemListener listener){
			this.startupListeners.add(listener);
			
			return this;
		}
		
		public RCEApplication build() throws RCEApplicationException{
			ServiceLoader<RCEApplicationFactory> loader = ServiceLoader.load(RCEApplicationFactory.class);
			
			for (RCEApplicationFactory factory: loader){
				factory.addSystemListeners(startupListeners);
				return factory.createApplication(configOverrideLocation);
			}
			
			throw new IllegalArgumentException("No service loader found.");
		}
	}
}
