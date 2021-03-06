package com.haines.ml.rce.accumulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haines.ml.rce.model.Event;
import com.haines.ml.rce.model.EventConsumer;

/**
 * The following represents a series of accumulators, stored as a trie structure.
 * As new events are assumed to be located spartially, with new accumulators being assigned
 * to slots incrementally indexed, the indexing to the trie is based on the higher bits of
 * the slot numbers. This leads to good spartial occupancy along with the ability for 
 * more frequently incremented accumulators to be located close to each other, making them
 * more cache friendly.
 * 
 * The default maximum number of accumulators that this can store is currently set at 
 * 2^24 (16,777,216 different accumulators) 
 * 
 * - This is based on 16KB accumulator lines and then 4096 accumulators in each line.
 * 
 * @author haines
 *
 * @param <T>
 */
public abstract class Accumulator<T extends Event> implements EventConsumer<T>, AccumulatorProvider<T>{

	private static final int DEFAULT_SECOND_LINE_LENGTH = 4096;
	private static final int DEFAULT_INDEX_LINE_LENGTH = 64;
	
	public static final AccumulatorConfig DEFAULT_CONFIG = new AccumulatorConfig() {
		
		@Override
		public int getFinalAccumulatorLineBitDepth() {
			return (int)(Math.log(DEFAULT_SECOND_LINE_LENGTH) / Math.log(2)); // 2^12
		}

		@Override
		public int getFirstAccumulatorLineBitDepth() {
			return (int)(Math.log(DEFAULT_INDEX_LINE_LENGTH) / Math.log(2));
		}

		@Override
		public int getSecondAccumulatorLineBitDepth() {
			return (int)(Math.log(DEFAULT_INDEX_LINE_LENGTH) / Math.log(2));
		}
	};

	private static final Logger LOG = LoggerFactory.getLogger(Accumulator.class);
	
	private final int[][][] accumulators;
	protected final AccumulatorLookupStrategy<? super T> lookup;
	private final int finalAccumulatorLineSize;
	private final int finalAccumulatorBitSize;
	private final int finalAccumulatorMask;
	private final int secondAccumualtorLineSize;
	private final int firstAccumulatorLineIndexMask;
	private final int secondAccumulatorLineIndexMask;
	private final int firstAccumulatorShift;
	private final int phyisicalLimitOfAccumulator;
	private final AccumulatorConfig config;
	private int currentMaxIdx = 0;
	
	public Accumulator(AccumulatorConfig config, AccumulatorLookupStrategy<? super T> lookup){
		
		this.config = config;
		int firstLineLength = getFirstLineMaxIndex(config);
		secondAccumualtorLineSize = getSecondLineMaxIndex(config);
		this.accumulators = newAccumulator(firstLineLength, secondAccumualtorLineSize);
		this.lookup = lookup;
		
		this.finalAccumulatorBitSize = config.getFinalAccumulatorLineBitDepth();
		this.finalAccumulatorLineSize = (int)Math.pow(2, finalAccumulatorBitSize);
		if (LOG.isDebugEnabled()){
			printAccumulatorConfig(firstLineLength, secondAccumualtorLineSize, finalAccumulatorLineSize);
		
		}
		this.finalAccumulatorMask = finalAccumulatorLineSize-1;
		this.firstAccumulatorShift = finalAccumulatorBitSize + config.getSecondAccumulatorLineBitDepth();
		this.firstAccumulatorLineIndexMask = (firstLineLength-1) << firstAccumulatorShift; // shift 18
		this.secondAccumulatorLineIndexMask = (secondAccumualtorLineSize-1) << finalAccumulatorBitSize; // shift 12
		this.phyisicalLimitOfAccumulator = (firstLineLength * secondAccumualtorLineSize * finalAccumulatorLineSize) - 1;
		
		this.currentMaxIdx = finalAccumulatorLineSize;
	}
	
	private int[][][] newAccumulator(int firstLineLength, int secondLineLength) {
		return new int[firstLineLength][secondLineLength][]; // 4096 * 32 bits memory footprint for structure (16KB)
	}

	private static void printAccumulatorConfig(int firstLineLength, int secondLineLength, int finalAccumulatorLineSize) {
		
		int totalLineSize = firstLineLength * secondLineLength;
		int totalAccumulatorSize = (totalLineSize*finalAccumulatorLineSize);
		LOG.debug("initalising multi dimensional accumulator with dimensions ["+firstLineLength+"]["+secondLineLength+"]["+finalAccumulatorLineSize+"].\n\tMinimal memory requirements = "+totalLineSize+" * 32bits = "+totalLineSize * 32+"\n\tMaximum memory requirements = "+totalAccumulatorSize+" * 32bits = "+(totalAccumulatorSize * 32));
	}

	private int getSecondLineMaxIndex(AccumulatorConfig config) {
		return (int)Math.pow(2, config.getSecondAccumulatorLineBitDepth());
	}

	private int getFirstLineMaxIndex(AccumulatorConfig config) {
		return (int)Math.pow(2, config.getFirstAccumulatorLineBitDepth());
	}

	public Accumulator(AccumulatorLookupStrategy<T> lookup){
		this(DEFAULT_CONFIG, lookup);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public AccumulatorLookupStrategy<T> getLookupStrategy() {
		return (AccumulatorLookupStrategy<T>)lookup;
	}
	
	public void incrementAccumulators(int[] slots){
		for (int i = 0; i < slots.length; i++){
			int slot = slots[i];
			if (slot > getMaxIndex()){
				LOG.warn("We are trying to update an accumulator which we dont have an index for. Rolling back event updates. slot {} into {}", slot, getMaxIndex());
				
				rollbackSlots(slots, i);
				return;
			}
			
			incrementAccumulator(slot);
		}
	}
	
	public void clear(){
		
		LOG.debug("clearing accumulator line for thread {}", Thread.currentThread().getName());
		int secondLineLength = getSecondLineMaxIndex(config);
		for (int i = 0; i < accumulators.length; i++){
			accumulators[i] = new int[secondLineLength][];
		}
		
		// now clear the indexes
		
		lookup.clear();
		
		this.currentMaxIdx = finalAccumulatorLineSize;
	}
	
	public void rollbackSlots(int[] slots, int idxToRollbackTo) {
		
		for (int i = 0; i < idxToRollbackTo; i++){
			int slot = slots[i];
			
			decrementAccumulator(slot);
		}
	}
	
	private void decrementAccumulator(int slot){
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		getAccumulatorLine(slot)[accumulatorIdx]--;
	}
	
	private final int getFirstAccumulatorLineIdx(int slot){
		return (slot & firstAccumulatorLineIndexMask) >> firstAccumulatorShift;//use most significant n bits for first index
	}
	
	private int[][] getFirstAccumulatorLine(int slot){
		int firstAccumulatorLineIdx = getFirstAccumulatorLineIdx(slot);
		
		return accumulators[firstAccumulatorLineIdx];
	}
	
	private int getSecondAccumulatorLineIdx(int slot){
		return (slot & secondAccumulatorLineIndexMask) >> finalAccumulatorBitSize;
	}
	
	private int[] getAccumulatorLine(int slot){
		
		int[][] firstAccumulatorLine = getFirstAccumulatorLine(slot);
		
		int secondAccumulatorLineId = getSecondAccumulatorLineIdx(slot);
		if (firstAccumulatorLine[secondAccumulatorLineId] == null){
			firstAccumulatorLine[secondAccumulatorLineId] = new int[finalAccumulatorLineSize];
			
			int potentialNewMaxIdx = getNewMaxId(getFirstAccumulatorLineIdx(slot), secondAccumulatorLineId);
			if (currentMaxIdx < potentialNewMaxIdx){
				currentMaxIdx = potentialNewMaxIdx; // add another line to the max idx size
			}
		}
		
		return firstAccumulatorLine[secondAccumulatorLineId];
	}
	
	private int getNewMaxId(int firstAccumulatorIdx, int secondAccumulatorIdx){
		return (firstAccumulatorIdx * secondAccumualtorLineSize * finalAccumulatorLineSize) + ((1+secondAccumulatorIdx) * finalAccumulatorLineSize);
	}

	/**
	 * Increment the slot at the specified index by 1.
	 * @param slot
	 */
	public void incrementAccumulator(int slot) {
		int accumulatorIdx = getAccumulatorIdx(slot);

		getAccumulatorLine(slot)[accumulatorIdx]++;
	}
	
	/**
	 * Increment the integer value at the specified slot by the specified valueToSum.
	 * @param slot
	 * @param valueToSum
	 */
	public void sumAccumulator(int slot, int valueToSum) {
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		getAccumulatorLine(slot)[accumulatorIdx] =+ valueToSum;
	}
	
	/**
	 * Increment the float value at the specified slot by the specified valueToSum.
	 * @param slot
	 * @param valueToSum
	 */
	public void sumAccumulator(int slot, float valueToSum){
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		getAccumulatorLine(slot)[accumulatorIdx] = Float.floatToIntBits(getAccumulatorValueAsFloat(slot) + valueToSum);
	}
	
	/**
	 * Returns the integrer value at the specified slot.
	 */
	public int getAccumulatorValue(int slot) {
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		return getAccumulatorLine(slot)[accumulatorIdx];
	}
	
	/**
	 * Returns the float value at the specified slot.
	 */
	public float getAccumulatorValueAsFloat(int slot) {
		return Float.intBitsToFloat(getAccumulatorValue(slot));
	}

	private int getAccumulatorIdx(int slot) {
		return (slot & finalAccumulatorMask);
	}

	/**
	 * Returns an accumulator provider that represents a copy of the data at the moment this method was invoked.
	 * @return
	 */
	public AccumulatorProvider<T> getAccumulatorProvider() {
		
		//LOG.debug("current maxId: {}", lookup.getMaxIndex());
		
		return new MemorySafeAccumulatorProvider<T>(accumulators, lookup.getMaxIndex(), finalAccumulatorLineSize, lookup);
	}
	
	/**
	 * Returns the maximum slot number that this accumulator can represent.
	 * @return
	 */
	public int getMaxIndex() {
		return phyisicalLimitOfAccumulator;
	}

	/**
	 * enforces a memory barrier so that other threads can read the contents
	 * of this accumulator
	 * @author haines
	 *
	 */
	private static class MemorySafeAccumulatorProvider<E extends Event> implements AccumulatorProvider<E>{

		private final int[] accumulators;
		private final int maxIndex;
		private final int finalAccumulatorLineSize;
		private final AccumulatorLookupStrategy<? super E> lookupStrategy;
		
		public MemorySafeAccumulatorProvider(int[][][] accumulators, int maxIndex, int finalAccumulatorLineSize, AccumulatorLookupStrategy<? super E> lookupStrategy) {
			maxIndex = maxIndex+1;
			
			int[] accumulatorArray = new int[maxIndex];
			
			this.finalAccumulatorLineSize = finalAccumulatorLineSize;
			deepCopy(accumulators, accumulatorArray, maxIndex);
			
			this.accumulators = accumulatorArray; 
			this.maxIndex = maxIndex;
			this.lookupStrategy = lookupStrategy.copy();
		}

		private void deepCopy(int[][][] src, int[] dest, int maxIdx) {
			
			int firstIdx = 0;
			int secondIdx = 0;
			int[][] firstLine = src[firstIdx++];
			int[] secondLine = null;
			
			for (int i = 0; i < maxIdx; i+=finalAccumulatorLineSize){

				
				if (secondIdx == firstLine.length){
					if (firstIdx == src.length){
						break;
					}
					firstLine = src[firstIdx++];
					secondIdx = 0;
				}
				
				secondLine = firstLine[secondIdx++];
				
				if (secondLine != null){	
					
					System.arraycopy(secondLine, 0, dest, i, Math.min((dest.length - i), secondLine.length));
				}
			}
		}

		@Override
		public int getAccumulatorValue(int slot){
			if (slot < maxIndex){
				return accumulators[slot];
			}
			return 0;
		}

		@Override
		public AccumulatorLookupStrategy<? super E> getLookupStrategy() {
			return lookupStrategy;
		}

		@Override
		public float getAccumulatorValueAsFloat(int slot) {
			return Float.intBitsToFloat(getAccumulatorValue(slot));
		}
	}
}