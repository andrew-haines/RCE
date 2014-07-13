package com.haines.ml.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import com.google.common.collect.Iterables;

/**
 * An iterable that caches constructed iterables. Use this if you have an iterator that performs a lot of computational work
 * to create each item for each iteration. The cache is only made available if a full iteration to the end has been completed.
 * 
 * Note that the supplied iterable is assumed but not enforced to be immutable and deterministic.
 * @author andrewhaines
 *
 * @param <E>
 */
public class CachedIterable<E> extends AbstractCollection<E>{
	
	private final Iterable<E> delegate;
	private Iterator<E> tempIterator;
	private final Collection<E> cache;
	/* 
	 * The following exists as it is very posible that a user may not iterate to the end of the iteration (errors etc). 
	 * If this temp store was not used then further invocations of iterations would result in incomplete results
	*/
	private final Deque<E> tempCache = new LinkedList<E>(); 
	private boolean finished = false;
	
	public CachedIterable(Iterable<E> delegate){
		this.delegate = delegate;
		if (delegate instanceof Collection){ // instances of collection are already cached some just assign reference
			this.cache = (Collection<E>)delegate;
			this.finished = true;
		} else{
			cache = new ArrayList<E>();
		}
	}

	@Override
	public Iterator<E> iterator() {
		if (finished){
			return cache.iterator();
		} else{
			if (tempIterator == null){
				tempIterator = delegate.iterator();
			}
			
			final Iterator<E> tempCacheIt = new ArrayList<E>(tempCache).iterator();
			
			return new Iterator<E>(){

				@Override
				public boolean hasNext() {
					boolean hasNext = tempCacheIt.hasNext() || tempIterator.hasNext();
					if (!hasNext){
						CachedIterable.this.finished = true;
						CachedIterable.this.cache.addAll(tempCache);
						tempCache.clear();
						tempIterator = null; // gc food
					}
					return hasNext;
				}

				@Override
				public E next() {
					
					if (tempCacheIt.hasNext()){
						return tempCacheIt.next();
					}
					E item = tempIterator.next();
					tempCache.addLast(item);
				
					return item;
				}

				@Override
				public void remove() {
					tempIterator.remove();
					tempCache.removeLast();
				}
			};
		}
	}
	private void fillCache(){
		Iterator<E> it = this.iterator();
		
		while(it.hasNext()){
			it.next();
		};
	}

	@Override
	public int size(){
		if (!finished){
			fillCache();
		}
		return cache.size();
	}

	@Override
	public boolean equals(Object obj){
		if (obj instanceof CachedIterable){
			CachedIterable<?> it = (CachedIterable<?>)obj;
			return Iterables.elementsEqual(this, it);
		}
		return false;
	}
	
	@Override
	public Object[] toArray(){
		if (!finished){
			super.toArray();	
		}
		return cache.toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a) {
		if (!finished){
			super.toArray(a);
		}
		return cache.toArray(a);
	}
}
