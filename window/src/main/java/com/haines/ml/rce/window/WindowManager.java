package com.haines.ml.rce.window;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.accumulator.HandlerRepository;
import com.haines.ml.rce.aggregator.Aggregator;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.naivebayes.CountsProviderNaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider.Counts;
import com.haines.ml.rce.naivebayes.NaiveBayesIndexes.NaiveBayesPosteriorDistributionProperty;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableDiscreteNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableNaiveBayesDistributionCounts;

public class WindowManager implements NaiveBayesProbabilitiesProvider{

	private final static Logger LOG = LoggerFactory.getLogger(WindowManager.class);
	
	private static final int NO_WINDOW_IDX = -1;
	private final WindowConfig config;
	private final WindowProbabilities windowProbabilities;
	
	private final Window[] cyclicWindowBuffer;
	private int currentMaxIdx = NO_WINDOW_IDX; // we can remove the need for volatile modifiers here as long as we can ensure that the single writer paradigm is enforced.
	private int currentMinIdx = 0;
	private final Clock clock;
	private final Iterable<? extends WindowUpdatedListener> staticWindowListeners;
	
	public WindowManager(WindowConfig config, Clock clock, Iterable<? extends WindowUpdatedListener> staticWindowListeners, HandlerRepository<?> featureHandlers){
		this.clock = clock;
		this.config = config;
		this.cyclicWindowBuffer = new Window[config.getNumWindows()];
		this.windowProbabilities = new WindowProbabilities(new SubtractableAggregator(new ConcurrentHashMap<Classification, Map<Feature, MutableDiscreteNaiveBayesCounts>>(), 
																				new ConcurrentHashMap<Classification, MutableDiscreteNaiveBayesCounts>(),
																				new ConcurrentHashMap<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts>(),
																				new ConcurrentHashMap<Integer, MutableNaiveBayesDistributionCounts>()), featureHandlers);
		
		this.staticWindowListeners = staticWindowListeners;
		
	}
	
	@Override
	public NaiveBayesProbabilities getProbabilities() {
		return windowProbabilities.getProbabilities();
	}
	
	/**
	 * This adds a new provider to the current window if it has not expired yet else it adds this provider as a new window.
	 * Note that if the window buffer fills up, this will remove the least freshest window in the buffer. This method
	 * is only thread safe if the single writer paradigm is used
	 * @param provider
	 */
	public void addNewProvider(NaiveBayesCountsProvider provider, WindowUpdatedListener listener){
		long currentTime = clock.getCurrentTime();
		
		if (LOG.isDebugEnabled()){
			//LOG.debug("Recieved: {}", provider);
		}
		
		int currentMaxIdx = this.currentMaxIdx; // cache friendly version. NOTE that this is not needed anymore as these variables are no longer volatile
		int currentMinIdx = this.currentMinIdx; 
		
		if (currentMaxIdx == NO_WINDOW_IDX || currentTime > cyclicWindowBuffer[currentMaxIdx].getExpires()){
		
			// TODO flush any windows that this new event skips past. Probably not so important now we have the heart beat triggers
			
			// shift to new window
			
			Window newWindow = null;
			
			if (currentMaxIdx != NO_WINDOW_IDX){
				newWindow = new Window(cyclicWindowBuffer[currentMaxIdx].getExpires() + config.getWindowPeriod(), provider);
			} else{
				newWindow = new Window(currentTime + config.getWindowPeriod(), provider);
			}
			
			Window oldWindow = null;
			if (getNextIdxInBuffer(currentMaxIdx) == currentMinIdx){ // the buffer is full
				oldWindow = cyclicWindowBuffer[currentMinIdx];
				
				// increment minIdx
				this.currentMinIdx = getNextIdxInBuffer(currentMinIdx);
			}
			
			//LOG.debug("New Window created at: {}", currentMaxIdx);
			
			currentMaxIdx = getNextIdxInBuffer(currentMaxIdx);
			this.currentMaxIdx = currentMaxIdx;
			cyclicWindowBuffer[currentMaxIdx] = newWindow;
			
			windowProbabilities.processWindows(newWindow.getProvider(), (oldWindow != null)? oldWindow.getProvider(): null);
			
			// update the listener only when there is a new window
			
			listener.newWindowCreated(this);
			
			for (WindowUpdatedListener staticListener: staticWindowListeners){
				staticListener.newWindowCreated(this);
			}
			
		} else { // add to existing window
			Window currentWindow = cyclicWindowBuffer[currentMaxIdx];
			
			Aggregator aggregatedWindow = Aggregator.newInstance();
			
			Counts currentWindowCounts = currentWindow.getProvider().getCounts();
			Counts newCounts = provider.getCounts();
			
			aggregatedWindow.aggregate(Iterables.concat(currentWindowCounts.getPosteriors(), 
														currentWindowCounts.getPriors()));
			
			aggregatedWindow.aggregate(Iterables.concat(newCounts.getPosteriors(), 
														newCounts.getPriors()));
			
			
			Window newAggregatedWindow = new Window(cyclicWindowBuffer[currentMaxIdx].getExpires(), aggregatedWindow);
			
			cyclicWindowBuffer[currentMaxIdx] = newAggregatedWindow;
			
			//LOG.debug("Window {} updated ", currentMaxIdx);
			
			windowProbabilities.processWindows(newAggregatedWindow.getProvider(), currentWindow.getProvider());
		}
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		
		builder.append("Windows: \n");
		int idx = 0;
		
		for (int i = currentMaxIdx; i != currentMinIdx; i = getPreviousIdxInBuffer(i)){
			builder.append("\tw[").append(idx++).append("] - ");
			if (cyclicWindowBuffer[i] != null){
				Counts counts = cyclicWindowBuffer[i].getProvider().getCounts();
				builder.append(Iterables.size(counts.getPosteriors()));
				builder.append("#[");
				builder.append(counts);
				builder.append("] :");
				builder.append(Iterables.size(counts.getPriors()));
				builder.append(" e=");
				builder.append(cyclicWindowBuffer[i].getExpires());
			} else{
				builder.append("null");
			}
			builder.append("\n");
		}
		
		return builder.toString();
	}

	private int getNextIdxInBuffer(int idx){
		return (idx + 1) % cyclicWindowBuffer.length;
	}
	
	private int getPreviousIdxInBuffer(int idx){
		return (idx == 0)?cyclicWindowBuffer.length-1:(idx - 1);
	}
	
	private static class Window{
		private final long expires;
		private final NaiveBayesCountsProvider provider;
		
		private Window(long expires, NaiveBayesCountsProvider provider){
			this.expires = expires;
			this.provider = provider;
		}

		public long getExpires() {
			return expires;
		}

		public NaiveBayesCountsProvider getProvider() {
			return provider;
		}
		
		@Override
		public String toString(){
			return "exp: "+expires+" - "+provider.toString();
		}
	}
	
	private static class WindowProbabilities{
		
		private final SubtractableAggregator aggregator;
		private volatile NaiveBayesProbabilities probabilities = NaiveBayesProbabilities.NOMINAL_PROBABILITIES;
		private final HandlerRepository<?> featureHandlers;
		
		private WindowProbabilities(SubtractableAggregator aggregator, HandlerRepository<?> featureHandlers){
			this.aggregator = aggregator;
			this.featureHandlers = featureHandlers;
		}

		private NaiveBayesProbabilities getProbabilities(){
			return probabilities;
		}
		
		private void processWindows(NaiveBayesCountsProvider newWindow, NaiveBayesCountsProvider oldWindow){

			Counts newWindowCounts = newWindow.getCounts();
			
			aggregator.add(newWindowCounts.getPosteriors());
			aggregator.add(newWindowCounts.getPriors());
			
			if (oldWindow != null){ // if the buffer has wrapped all the way around and we have all windows full then subtract the first element in cyclic window buffer
				Counts oldWindowCounts = oldWindow.getCounts();
				aggregator.subtract(oldWindowCounts.getPosteriors());
				aggregator.subtract(oldWindowCounts.getPriors());
			}
			
			probabilities = new CountsProviderNaiveBayesProbabilities(aggregator, featureHandlers);
		}
	}
	
	private static class SubtractableAggregator extends Aggregator{

		public SubtractableAggregator(Map<Classification, Map<Feature, MutableDiscreteNaiveBayesCounts>> discretePosteriorCounts, Map<Classification, MutableDiscreteNaiveBayesCounts> discretePriorCounts, Map<NaiveBayesPosteriorDistributionProperty, MutableNaiveBayesDistributionCounts> distributionPosteriorCounts, Map<Integer, MutableNaiveBayesDistributionCounts> distributionPriorCounts) {
			super(discretePosteriorCounts, discretePriorCounts, distributionPosteriorCounts, distributionPriorCounts);
		}

		private void subtract(Iterable<? extends NaiveBayesCounts<?>> counts){
			aggregate(counts, true);
		}
		
		private void add(Iterable<? extends NaiveBayesCounts<?>> counts){
			aggregate(counts, false);
		}
	}
}
