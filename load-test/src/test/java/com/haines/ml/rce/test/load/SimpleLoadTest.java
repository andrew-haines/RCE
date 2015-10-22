package com.haines.ml.rce.test.load;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;

import net.grinder.common.GrinderException;
import net.grinder.common.GrinderProperties;
import net.grinder.console.ConsoleFoundation;
import net.grinder.console.common.ResourcesImplementation;
import net.grinder.engine.agent.Agent;
import net.grinder.engine.agent.AgentDaemon;
import net.grinder.engine.agent.AgentImplementation;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.jtmb.grinderAnalyzer.MDC;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.python.Version;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.accumulator.handlers.ClassificationHandler;
import com.haines.ml.rce.accumulator.handlers.FeatureHandler;
import com.haines.ml.rce.accumulator.handlers.SequentialDistributionFeatureHandler;
import com.haines.ml.rce.main.RCEApplicationException;
import com.haines.ml.rce.main.RCEApplicationStartupTest;
import com.haines.ml.rce.main.factory.FeatureHandlerRepositoryFactory;
import com.haines.ml.rce.model.ClassifiedEvent;
import com.haines.ml.rce.test.data.SyntheticTestDataset;

public class SimpleLoadTest extends RCEApplicationStartupTest {

	private static final Logger LOG = LoggerFactory.getLogger(SimpleLoadTest.class);
	
	private static final int NUM_FEATURES = 10;
	
	private Agent agent;
	private final ConsoleFoundation consoleFoundation;
	public final static SyntheticTestDataset DATASET = new SyntheticTestDataset(3, NUM_FEATURES, 0.6);

	private static final Pattern LOAD_TEST_DATA_FILE = Pattern.compile(".*localhost-.*?-data\\.log.*");
	private static final Pattern LOAD_TEST_LOG_FILE = Pattern.compile(".*localhost-.*[^-data]\\.log.*");
	
	private ExecutorService executor;
	private GrinderMessagingInspector inspector;
	private final Path grinderPropertyFile;
	
	public SimpleLoadTest() throws GrinderException, URISyntaxException {
		this.consoleFoundation = new ConsoleFoundation(new ResourcesImplementation("net.grinder.console.common.resources.Console"), LoggerFactory.getLogger("grinder"), true);
		
		this.grinderPropertyFile = Paths.get(SimpleLoadTest.class.getResource("/"+GrinderProperties.DEFAULT_PROPERTIES).toURI());
	}
	
	@Before
	public void setUpGrinderAgent() throws GrinderException, InterruptedException, RCEApplicationException, JAXBException, IOException{
		
		this.startUpRCE(getFeatureHandlerRepositoryFactory());
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
	protected boolean isUsingSlf4jEventListener() {
		return false;
	}

	// for efficiency, override these test methods to stop them re running. TODO should really to in a separate test
	@Override
	public void givenCandidateAndTCPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly(){}
	@Override
	public void givenCandidateAndUDPClient_whenCallingStartAndSendingEventsViaSelector_thenApplicationStartsUpCorrectly(){}
	
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
		
		clearPreviousDataRuns();
		
		Path tmpDirectory = Files.createTempDirectory("GrinderAnalyzer");
		setUpAnalyzer(tmpDirectory);
		
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
		
		Thread.sleep(100000);
		
		inspector.getProcessControl().stopAgentAndWorkerProcesses();
		
		agent.shutdown();
		
		LOG.info("analysing results");
		
		Version.PY_VERSION = "2.5.2"; // pretend we are an older version
		
		try(PythonInterpreter pi = new PythonInterpreter()){
		
			org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger("analyzer");
			
			logger.setLevel(Level.ALL);
			logger.addAppender(new WriterAppender(new PatternLayout("%d{ISO8601} %-5p [%c{1}] - %m%n"), System.out));
			
			MDC.put("current.dir", tmpDirectory.toFile().getAbsolutePath());
			
			pi.setErr(System.err);
			pi.setOut(System.out);
			pi.getSystemState().argv.add(Joiner.on(' ').join(getFiles(LOAD_TEST_DATA_FILE, true)));
			pi.getSystemState().argv.add(Iterables.get(getFiles(LOAD_TEST_LOG_FILE), 0));
			
			
			pi.execfile(SimpleLoadTest.class.getResourceAsStream("/analyzer.py"));
		}
	}

	private void clearPreviousDataRuns() throws URISyntaxException, IOException {
		for (String file: Iterables.concat(getFiles(LOAD_TEST_DATA_FILE), getFiles(LOAD_TEST_LOG_FILE))){
			Files.delete(Paths.get(file));
		}
	}
	
	private Iterable<String> getFiles(final Pattern regex) throws URISyntaxException, IOException {
		return getFiles(regex, false);
	}

	private Iterable<String> getFiles(final Pattern regex, final boolean checkSize) throws URISyntaxException, IOException {
		
		Path root = Paths.get(SimpleLoadTest.class.getResource("/").toURI()).getParent().getParent();
		
		return Iterables.transform(Files.newDirectoryStream(root, new DirectoryStream.Filter<Path>() {

			@Override
			public boolean accept(Path entry) throws IOException {
				return (!checkSize || Files.size(entry) > 8000) && regex.matcher(entry.toString()).matches();
			}
		}), new Function<Path, String>(){

			@Override
			public String apply(Path input) {
				return input.toFile().getName();
			}
			
		});
	}

	private void setUpAnalyzer(Path tmpDirectory) throws IOException {
		
		Path conf = Files.createDirectory(Paths.get(tmpDirectory.toFile().getAbsolutePath(), "conf"));
		
		LOG.info("Copying configuration to: "+conf);
		
		Path confFile = Files.createFile(Paths.get(conf.toFile().getAbsolutePath(), "analyzer.properties"));
		
		copyFromClassPath(confFile, "/conf/analyzer.properties");
		
		Collection<String> templateFiles = new ArrayList<String>();
		
		for (ResourceInfo resource: ClassPath.from(SimpleLoadTest.class.getClassLoader()).getResources()){
			
			if (resource.getResourceName().startsWith("templates/")){
				templateFiles.add(resource.getResourceName());
			}
		}
		
		Path templates = Files.createDirectory(Paths.get(tmpDirectory.toFile().getAbsolutePath(), "templates"));
		
		for(String templateFile: templateFiles){
			
			Path tempFile = Files.createFile(Paths.get(templates.toFile().getAbsolutePath(), templateFile.substring(10, templateFile.length())));
			
			LOG.debug("copying {} from classpath to local file {}", templateFile, tempFile);
			copyFromClassPath(tempFile, SimpleLoadTest.class.getResourceAsStream("/"+templateFile));
		}
	}
	
	private void copyFromClassPath(Path destFile, InputStream file) throws FileNotFoundException, IOException {
		try(OutputStream stream = new FileOutputStream(destFile.toFile())){
			IOUtils.copy(file, stream);
		};
	}

	private void copyFromClassPath(Path destFile, String fileInClasspath) throws FileNotFoundException, IOException {
		copyFromClassPath(destFile, SimpleLoadTest.class.getResourceAsStream(fileInClasspath));
		
		LOG.info("copied: {} from classpath to local file {}", fileInClasspath, destFile);
	}
}
