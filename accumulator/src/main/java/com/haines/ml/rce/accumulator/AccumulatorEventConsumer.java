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

	private final Logger LOG = LoggerFactory.getLogger(AccumulatorEventConsumer.class);
	
	private final int[][][] accumulators;
	private final AccumulatorLookupStrategy<T> lookup;
	
	public AccumulatorEventConsumer(AccumulatorConfig config, AccumulatorLookupStrategy<T> lookup){
		this.accumulators = new int[64][64][]; // 4096 * 32 bits memory footprint for structure (16KB)
		this.lookup = lookup;
	}
	
	@Override
	public void consume(T event) {
		
		int[] slots = lookup.getSlots(event);
		
		for (int i = 0; i < slots.length; i++){
			int slot = slots[i];
			if (slot > lookup.getMaxIndex()){
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

	public int getAccumulatorValue(int slot){
		
		if (slot > lookup.getMaxIndex()){
			return 0;
		}
		int[][] firstAccumulatorLine = getFirstAccumulatorLine(slot);
		
		int[] accumulatorLine = firstAccumulatorLine[getSecondAccumulatorLineIdx(slot)];
		if (accumulatorLine == null){
			return 0;
		}
		
		return accumulatorLine[getAccumulatorIdx(slot)];
	}
	
	private int[][] getFirstAccumulatorLine(int slot){
		int firstAccumulatorLineIdx = (slot & 0xFC0000) >> 18;//use most significant 64 bits for first index
		
		return accumulators[firstAccumulatorLineIdx];
	}
	
	private int getSecondAccumulatorLineIdx(int slot){
		return (slot & 0x3F000) >> 12;
	}
	
	private int[] getAccumulatorLine(int slot){
		
		int[][] firstAccumulatorLine = getFirstAccumulatorLine(slot);
		
		int secondAccumulatorLineId = getSecondAccumulatorLineIdx(slot);
		if (firstAccumulatorLine[secondAccumulatorLineId] == null){
			firstAccumulatorLine[secondAccumulatorLineId] = new int[4096];
		}
		
		return firstAccumulatorLine[secondAccumulatorLineId];
	}

	private void incrementAccumulator(int slot) {
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		getAccumulatorLine(slot)[accumulatorIdx]++;
		
	}

	private int getAccumulatorIdx(int slot) {
		return (slot & 0xFFF);
	}
}