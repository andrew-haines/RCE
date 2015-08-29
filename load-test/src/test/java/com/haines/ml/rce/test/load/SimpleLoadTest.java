package com.haines.ml.rce.test.load;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.console.ConsoleFoundation;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentDaemon;
import net.grinder.engine.agent.AgentImplementation;
import net.grinder.util.Directory;

import org.junit.After;
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
	private ExecutorService executor;
	private GrinderMessagingInspector inspector;
	private final Path grinderPropertyFile;
	
	public SimpleLoadTest() throws GrinderException, URISyntaxException {
		this.dataset = new SyntheticTestDataset(3, NUM_FEATURES, 0.6);
		this.consoleFoundation = new ConsoleFoundation(new ResourcesImplementation("net.grinder.console.common.resources.Console"), LOG, true);
		
		this.grinderPropertyFile = Paths.get(SimpleLoadTest.class.getResource("/"+GrinderProperties.DEFAULT_PROPERTIES).toURI());
	}
	
	@Before
	public void setUpGrinderAgent() throws GrinderException, InterruptedException{
		agent = new AgentDaemon(
				  LOG,
		          100,
		          new AgentImplementation(LOG, grinderPropertyFile.toFile(), false));
		
		executor = Executors.newFixedThreadPool(2, new ThreadFactory(){

			private int threadNum = 0;
			
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r, "LoadTestDriveThread_"+threadNum++);
			}
			
		});
		
		executor.execute(new Runnable(){

			@Override
			public void run() {
				consoleFoundation.run();
			}
		});
		
		Thread.sleep(1000);
		inspector = GrinderMessagingInspector.getInstance();
	}
	
	@Override
	public void givenCandidate_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly(){}
	
	@Override
	public void givenCandidate_whenCallingStart_thenApplicationStartsUpCorrectly(){}
	
	@Override
	public void givenRCEApplication_whenTrainedWithSimpleSyntheticData_thenClassifierWorksAsExpected(){}
	
	@After
	public void shutdownGrinderConsole(){
		
		consoleFoundation.shutdown();
		
		executor.shutdown();
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
	public void runLoadTest() throws GrinderException, IOException, URISyntaxException, InterruptedException{
		executor.execute(new Runnable(){

			@Override
			public void run() {
				try {
					agent.run();
				} catch (GrinderException e) {
					throw new RuntimeException("unable to start agents", e);
				}
			}
		});
		
		
		
		Thread.sleep(1000);
		
		inspector.getProcessControl().startWorkerProcesses(new GrinderProperties());
		
		System.in.read();
	}
}
