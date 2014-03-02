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
public class AccumulatorEventConsumer<T extends Event> implements EventConsumer<T>{

	private static final int DEFAULT_SECOND_LINE_LENGTH = 4096;
	
	private static final AccumulatorConfig DEFAULT_CONFIG = new AccumulatorConfig() {
		
		@Override
		public int getFinalAccumulatorLineBitDepth() {
			return (int)(Math.log(DEFAULT_SECOND_LINE_LENGTH) / Math.log(2)); // 12?
		}
	};

	private static final Logger LOG = LoggerFactory.getLogger(AccumulatorEventConsumer.class);
	
	private final int[][][] accumulators;
	private final AccumulatorLookupStrategy<T> lookup;
	private final int finalAccumulatorLineSize;
	private final int finalAccumulatorBitSize;
	private final int finalAccumulatorMask;
	private final int firstAccumulatorLineIndexMask;
	private final int secondAccumulatorLineIndexMask;
	private final int firstAccumulatorShift;
	private final int phyisicalLimitOfAccumulator;
	
	public AccumulatorEventConsumer(AccumulatorConfig config, AccumulatorLookupStrategy<T> lookup){
		this.accumulators = new int[64][64][]; // 4096 * 32 bits memory footprint for structure (16KB)
		this.lookup = lookup;
		
		this.finalAccumulatorBitSize = config.getFinalAccumulatorLineBitDepth();
		this.finalAccumulatorLineSize = (int)Math.pow(2, finalAccumulatorBitSize);
		this.finalAccumulatorMask = finalAccumulatorLineSize-1;
		this.firstAccumulatorShift = finalAccumulatorBitSize + 6;
		this.firstAccumulatorLineIndexMask = 0x3F << firstAccumulatorShift; // shift 18
		this.secondAccumulatorLineIndexMask = 0x3F << finalAccumulatorBitSize; // shift 12
		this.phyisicalLimitOfAccumulator = 64 * 64 * finalAccumulatorLineSize;
	}
	
	public AccumulatorEventConsumer(AccumulatorLookupStrategy<T> lookup){
		this(DEFAULT_CONFIG, lookup);
	}
	
	@Override
	public void consume(T event) {
		
		int[] slots = lookup.getSlots(event);
		
		for (int i = 0; i < slots.length; i++){
			int slot = slots[i];
			if (slot > lookup.getMaxIndex() || slot > phyisicalLimitOfAccumulator){
				LOG.warn("We are trying to update an accumulator which we dont have an index for. Rolling back event updates");
				
				rollbackSlots(slots, i);
				return;
			}
			incrementAccumulator(slot);
		}
	}
	
	private void rollbackSlots(int[] slots, int idxToRollbackTo) {
		
		for (int i = 0; i < idxToRollbackTo; i++){
			int slot = slots[i];
			
			decrementAccumulator(slot);
		}
	}
	
	private void decrementAccumulator(int slot){
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		getAccumulatorLine(slot)[accumulatorIdx]--;
	}
	
	private int[][] getFirstAccumulatorLine(int slot){
		int firstAccumulatorLineIdx = (slot & firstAccumulatorLineIndexMask) >> firstAccumulatorShift;//use most significant 64 bits for first index
		
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
		}
		
		return firstAccumulatorLine[secondAccumulatorLineId];
	}

	private void incrementAccumulator(int slot) {
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		getAccumulatorLine(slot)[accumulatorIdx]++;
		
	}

	private int getAccumulatorIdx(int slot) {
		return (slot & finalAccumulatorMask);
	}

	public AccumulatorProvider getAccumulatorProvider() {
		
		return new MemorySafeAccumulatorProvider(accumulators, lookup.getMaxIndex(), finalAccumulatorLineSize);
	}
	
	/**
	 * enforces a memory barrier so that other threads can read the contents
	 * of this accumulator
	 * @author haines
	 *
	 */
	private static class MemorySafeAccumulatorProvider implements AccumulatorProvider{

		private volatile int[] accumulators;
		private final int maxIndex;
		private final int finalAccumulatorLineSize;
		
		public MemorySafeAccumulatorProvider(int[][][] accumulators, int maxIndex, int finalAccumulatorLineSize) {
			this.accumulators = new int[maxIndex];
			
			this.finalAccumulatorLineSize = finalAccumulatorLineSize;
			deepCopy(accumulators, this.accumulators, maxIndex);
			
			this.maxIndex = maxIndex;
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
				
					System.arraycopy(secondLine, 0, dest, i, secondLine.length);
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
	}

}