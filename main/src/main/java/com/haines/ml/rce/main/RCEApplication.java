package com.haines.ml.rce.main;

import java.io.IOException;
import java.util.ServiceLoader;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.haines.ml.rce.eventstream.EventStreamException;
import com.haines.ml.rce.eventstream.SelectorEventStream;
import com.haines.ml.rce.main.factory.GuiceRCEApplicationFactory;
import com.haines.ml.rce.main.factory.RCEApplicationFactory;
import com.haines.ml.rce.main.guice.RCEConfigConfiguredInitiationModule;

public class RCEApplication {
	
	private final SelectorEventStream<?> eventStream;
	
	public RCEApplication(SelectorEventStream<?> eventStream){
		this.eventStream = eventStream;
	}

	private void start() throws RCEApplicationException {
		try {
			eventStream.start();
		} catch (EventStreamException e) {
			throw new RCEApplicationException("unable to start RCE", e);
		}
	}
	
	private static final String CONFIG_OVERRIDE_OPTION_KEY = "configOverrideLocation";

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
		
		private RCEApplicationBuilder(String configOverrideLocation){
			this.configOverrideLocation = configOverrideLocation;
		}
		
		public RCEApplication build() throws RCEApplicationException{
			ServiceLoader<RCEApplicationFactory> loader = ServiceLoader.load(RCEApplicationFactory.class);
			
			for (RCEApplicationFactory factory: loader){
				return factory.createApplication();
			}

			return new GuiceRCEApplicationFactory(new RCEConfigConfiguredInitiationModule(configOverrideLocation)).createApplication();
		}
	}
}
