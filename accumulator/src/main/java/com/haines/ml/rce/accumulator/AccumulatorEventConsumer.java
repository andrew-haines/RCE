package com.haines.ml.rce.accumulator;

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
 * The maximum number of accumulators that this can store is currently set at 2^16 (65536)
 * 
 * @author haines
 *
 * @param <T>
 */
public class AccumulatorEventConsumer<T extends Event> implements EventConsumer<T>{

	private final int[][][] accumulators;
	private final AccumulatorLookupStrategy<T> lookup;
	
	public AccumulatorEventConsumer(AccumulatorConfig config, AccumulatorLookupStrategy<T> lookup){
		accumulators = new int[16][16][]; // total of 65536 accumulators.
		this.lookup = lookup;
	}
	
	@Override
	public void consume(T event) {
		
		int[] slots = lookup.getSlots(event);
		
		for (int slot: slots){
			incrementAccumulator(slot);
		}
	}
	
	public int getAccumulatorValue(int slot){
		int[][] firstAccumulatorLine = getFirstAccumulatorLine(slot);
		
		int[] accumulatorLine = firstAccumulatorLine[getSecondAccumulatorLineIdx(slot)];
		if (accumulatorLine == null){
			return 0;
		}
		
		return accumulatorLine[getAccumulatorIdx(slot)];
	}
	
	private int[][] getFirstAccumulatorLine(int slot){
		int firstAccumulatorLineIdx = (slot & 0xF000) >> 12;
		
		return accumulators[firstAccumulatorLineIdx];
	}
	
	private int getSecondAccumulatorLineIdx(int slot){
		return (slot & 0xF00) >> 8;
	}

	private void incrementAccumulator(int slot) {
		int accumulatorIdx = getAccumulatorIdx(slot);
		
		int[][] firstAccumulatorLine = getFirstAccumulatorLine(slot);
		
		int secondAccumulatorLineId = getSecondAccumulatorLineIdx(slot);
		if (firstAccumulatorLine[secondAccumulatorLineId] == null){
			firstAccumulatorLine[secondAccumulatorLineId] = new int[256];
		}
		
		firstAccumulatorLine[secondAccumulatorLineId][accumulatorIdx]++;
		
	}

	private int getAccumulatorIdx(int slot) {
		return (slot & 0xFF);
	}
}