package com.haines.ml.rce.window;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

import com.google.common.collect.Iterables;
import com.haines.ml.rce.aggregator.Aggregator;
import com.haines.ml.rce.model.Classification;
import com.haines.ml.rce.model.Feature;
import com.haines.ml.rce.model.system.Clock;
import com.haines.ml.rce.naivebayes.CountsProviderNaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.NaiveBayesCountsProvider;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilities;
import com.haines.ml.rce.naivebayes.NaiveBayesProbabilitiesProvider;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesCounts.MutableNaiveBayesCounts;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPosteriorProperty;
import com.haines.ml.rce.naivebayes.model.NaiveBayesProperty.NaiveBayesPriorProperty;

public class WindowManager implements NaiveBayesProbabilitiesProvider{

	private static final int NO_WINDOW_IDX = -1;
	private final WindowConfig config;
	private final WindowProbabilities windowProbabilities;
	
	private final Window[] cyclicWindowBuffer;
	private int currentMaxIdx = NO_WINDOW_IDX; // we can remove the need for volatile modifiers here as long as we can ensure that the single writer paradigm is enforced.
	private int currentMinIdx = 0;
	private final Clock clock;
	
	public WindowManager(WindowConfig config, Clock clock){
		this.clock = clock;
		this.config = config;
		this.cyclicWindowBuffer = new Window[config.getNumWindows()];
		this.windowProbabilities = new WindowProbabilities(new SubtractableAggregator(new THashMap<Classification, Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>>(), 
																				new THashMap<Classification, MutableNaiveBayesCounts<NaiveBayesPriorProperty>>()));
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
		
		int currentMaxIdx = this.currentMaxIdx; // cache friendly version. NOTE that this is not needed anymore as these variables are no longer volatile
		int currentMinIdx = this.currentMinIdx; 
		
		if (currentMaxIdx == NO_WINDOW_IDX || currentTime > cyclicWindowBuffer[currentMaxIdx].getExpires()){
			
			// TODO flush any windows that this new event skips past
			
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
			
			currentMaxIdx = getNextIdxInBuffer(currentMaxIdx);
			this.currentMaxIdx = currentMaxIdx;
			cyclicWindowBuffer[currentMaxIdx] = newWindow;
			
			windowProbabilities.processWindows(newWindow.getProvider(), (oldWindow != null)? oldWindow.getProvider(): null);
			
			// update the listener only when there is a new window
			
			listener.windowUpdated(this);
			
		} else { // add to existing window
			Window currentWindow = cyclicWindowBuffer[currentMaxIdx];
			
			Aggregator aggregatedWindow = Aggregator.newInstance();
			
			aggregatedWindow.aggregate(Iterables.concat(currentWindow.getProvider().getPosteriorCounts(), 
														currentWindow.getProvider().getPriorCounts()));
			
			aggregatedWindow.aggregate(Iterables.concat(provider.getPosteriorCounts(), 
														provider.getPriorCounts()));
			
			
			Window newAggregatedWindow = new Window(cyclicWindowBuffer[currentMaxIdx].getExpires(), aggregatedWindow);
			
			cyclicWindowBuffer[currentMaxIdx] = newAggregatedWindow;
			
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
				builder.append(Iterables.size(cyclicWindowBuffer[i].getProvider().getPosteriorCounts()));
				builder.append(":");
				builder.append(Iterables.size(cyclicWindowBuffer[i].getProvider().getPriorCounts()));
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
	}
	
	private static class WindowProbabilities{
		
		private final SubtractableAggregator aggregator;
		private volatile NaiveBayesProbabilities probabilities = NaiveBayesProbabilities.NOMINAL_PROBABILITIES;
		
		private WindowProbabilities(SubtractableAggregator aggregator){
			this.aggregator = aggregator;
		}

		private NaiveBayesProbabilities getProbabilities(){
			return probabilities;
		}
		
		private void processWindows(NaiveBayesCountsProvider newWindow, NaiveBayesCountsProvider oldWindow){

			aggregator.add(newWindow.getPosteriorCounts());
			aggregator.add(newWindow.getPriorCounts());
			
			if (oldWindow != null){ // if the buffer has wrapped all the way around and we have all windows full then subtract the first element in cyclic window buffer
				aggregator.subtract(oldWindow.getPosteriorCounts());
				aggregator.subtract(oldWindow.getPriorCounts());
			}
			
			probabilities = new CountsProviderNaiveBayesProbabilities(aggregator);
		}
	}
	
	private static class SubtractableAggregator extends Aggregator{

		public SubtractableAggregator(Map<Classification, Map<Feature, MutableNaiveBayesCounts<NaiveBayesPosteriorProperty>>> posteriorCounts,
				Map<Classification, MutableNaiveBayesCounts<NaiveBayesPriorProperty>> priorCounts) {
			super(posteriorCounts, priorCounts);
		}

		private void subtract(Iterable<? extends NaiveBayesCounts<? extends NaiveBayesProperty>> counts){
			aggregate(counts, true);
		}
		
		private void add(Iterable<? extends NaiveBayesCounts<? extends NaiveBayesProperty>> counts){
			aggregate(counts, false);
		}
	}
}
