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

import com.google.common.collect.Iterables;
import com.haines.ml.rce.eventstream.EventStreamController;
import com.haines.ml.rce.eventstream.EventStreamException;
import com.haines.ml.rce.main.config.RCEConfig;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.main.factory.RCEApplicationFactory;
import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;
import com.haines.ml.rce.model.system.SystemListener;
import com.haines.ml.rce.model.system.SystemStartedListener;
import com.haines.ml.rce.model.system.SystemStoppedListener;

/**
 * An interface that represents a single instance of the entire application. It simply exists to allow clients to start
 * and shut it down with an additional 'backdoor' method to inject events into the system directly. The later functionality is
 * only really intended for testing although other event passing harnesses can be written to act as event injectors.
 * @author haines
 *
 * @param <E>
 */
public interface RCEApplication<E extends Event> {
	
	/**
	 * Starts a the application
	 * @throws RCEApplicationException
	 */
	void start() throws RCEApplicationException;
	
	/**
	 * Stops the application
	 * 
	 * @throws RCEApplicationException
	 */
	void stop() throws RCEApplicationException;
	
	/**
	 * Returns the underlying event consumer to inject events manually into the system
	 * @return
	 */
	EventConsumer<E> getEventConsumer();
	
	/**
	 * Returns the {@link RCEConfig} object used to configure this application instance.
	 * @return
	 */
	RCEConfig getConfig();

	public static class DefaultRCEApplication<E extends Event> implements RCEApplication<E>{
		
		private final EventStreamController eventStream;
		private final EventConsumer<E> consumer;
		private final RCEConfig config;
		private final Collection<SystemListener> systemListeners;
		
		@Inject
		public DefaultRCEApplication(EventStreamController eventStream, EventConsumer<E> consumer, RCEConfig config, Collection<SystemListener> systemListeners){
			this.eventStream = eventStream;
			this.consumer = consumer;
			this.config = config;
			this.systemListeners = systemListeners;
		}
	
		@Override
		public void start() throws RCEApplicationException {
			try {
				eventStream.start();
				
				for(SystemStartedListener listener: Iterables.filter(systemListeners, SystemStartedListener.class)){
					listener.systemStarted();
				}
			} catch (EventStreamException e) {
				throw new RCEApplicationException("Unable to start RCE", e);
			}
		}
		
		@Override
		public void stop() throws RCEApplicationException{
			try {
				eventStream.stop();
				
				for(SystemStoppedListener listener: Iterables.filter(systemListeners, SystemStoppedListener.class)){
					listener.systemStopped();
				}
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
	
	        @SuppressWarnings("rawtypes")
			RCEApplicationBuilder<?> builder = new RCEApplicationBuilder(cmd.getOptionValue(CONFIG_OVERRIDE_OPTION_KEY));
	        
	        RCEApplication<?> application = builder.build();
	        
	        application.start();
		}

		@Override
		public EventConsumer<E> getEventConsumer() {
			return consumer;
		}

		@Override
		public RCEConfig getConfig() {
			return config;
		}
	}
	
	/**
	 * A builder for creating instances of {@link RCEApplication}.
	 * @author haines
	 *
	 * @param <T>
	 */
	public static class RCEApplicationBuilder<T extends Event> {
		
		// set the number of event worker to be 1 less than the number of CPUs on the VM
		private String configOverrideLocation;
		private Collection<SystemListener> startupListeners = new ArrayList<SystemListener>();
		private RCEConfig config = null;
		private FeatureHandlerRepositoryFactory featureHandlerRepo;
		private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		public RCEApplicationBuilder(String configOverrideLocation){
			this.configOverrideLocation = configOverrideLocation;
		}
		
		public RCEApplicationBuilder<T> addSystemStartedListener(SystemListener listener){
			this.startupListeners.add(listener);
			
			return this;
		}
		
		public RCEApplicationBuilder<T> setConfig(RCEConfig config){
			this.config = config;
			
			return this;
		}
		
		public RCEApplicationBuilder<T> setClassLoader(ClassLoader classLoader){
			this.classLoader = classLoader;
			
			return this;
		}
		
		public RCEApplicationBuilder<T> setHandlerRepositoryFactory(FeatureHandlerRepositoryFactory featureHandlerRepo){
			this.featureHandlerRepo = featureHandlerRepo;
			
			return this;
		}
		
		public RCEApplication<T> build() throws RCEApplicationException{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			ServiceLoader<RCEApplicationFactory<T>> loader = (ServiceLoader)ServiceLoader.load(RCEApplicationFactory.class, classLoader);
			
			for (RCEApplicationFactory<T> factory: loader){
				factory.addSystemListeners(startupListeners);
				if (config != null){
					factory.useSpecificConfig(config);
				}
				
				if (featureHandlerRepo != null){
					factory.useSpecificHandlerRepository(featureHandlerRepo);
				}
				
				return factory.createApplication(configOverrideLocation);
			}
			
			throw new IllegalArgumentException("No service loader found.");
		}
	}
}
