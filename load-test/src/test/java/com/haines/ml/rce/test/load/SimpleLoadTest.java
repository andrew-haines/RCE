package com.haines.ml.rce.test.load;

import java.util.Map;

import net.grinder.common.GrinderException;
import net.grinder.console.ConsoleFoundation;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentDaemon;
import net.grinder.engine.agent.AgentImplementation;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.accumulator.handlers.SequentialDistributionFeatureHandler;
import com.haines.ml.rce.main.RCEApplicationStartupTest;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.data.SyntheticTestDataset;

public class SimpleLoadTest extends RCEApplicationStartupTest {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleLoadTest.class);
	
	private static final int NUM_FEATURES = 10;
	
	private Agent agent;
	private final ConsoleFoundation consoleFoundation;
	private final SyntheticTestDataset dataset;
	
	public SimpleLoadTest() throws GrinderException {
		this.dataset = new SyntheticTestDataset(3, NUM_FEATURES, 0.6);
		this.consoleFoundation = new ConsoleFoundation(new ResourcesImplementation("net.grinder.console.common.resources.Console"), LOG, true);
	}
	
	@Before
	public void setUpGrinderAgent() throws GrinderException{
		agent = new AgentDaemon(
				  LOG,
		          100,
		          new AgentImplementation(LOG, null, false));
		
		consoleFoundation.run();
	}
	
	@Override
	protected FeatureHandlerRepositoryFactory getFeatureHandlerRepositoryFactory() {
		return new FeatureHandlerRepositoryFactory() {
			
			@Override
			public <E extends ClassifiedEvent> HandlerRepository<E> create() {
				
				ImmutableMap.Builder<Integer, FeatureHandler<E>> featureHandlers = new ImmutableMap.Builder<Integer, FeatureHandler<E>>();
				
				for (int i = 0; i < NUM_FEATURES; i++){
					featureHandlers.put(i, new SequentialDistributionFeatureHandler<E>());
				}
				Map<Integer, ClassificationHandler<E>> classificationHandlers = new ImmutableMap.Builder<Integer, ClassificationHandler<E>>().build();
				
				return HandlerRepository.create(featureHandlers.build(), classificationHandlers);
			}
		};
	}
	
	@Test
	public void runLoadTest() throws GrinderException{
		agent.run();
	}
}
