package com.haines.ml.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class CachedIterableUnitTest {
	
	private static final List<String> TEST_LIST = Arrays.asList("ITEM2", "ITEM5", "ITEM1", "ITEM2");
	private static final List<String> TEST_LIST_DIFFERENT = Arrays.asList("ITEM2", "ITEM4", "ITEM1", "ITEM2");
 	private static final List<String> TEST_LIST_DIFFERENT_LENGTH = Arrays.asList("ITEM2", "ITEM4", "ITEM1", "ITEM2", "ITEM8");
	
	private CachedIterable<String> candidate;
	
	@Mock
	private Iterable<String> iterationAwareIterable;
	
	@Before
	public void before(){
		iterationAwareIterable = mock(Iterable.class);
		candidate = new CachedIterable<String>(iterationAwareIterable);
		when(iterationAwareIterable.iterator()).thenAnswer(new Answer<Iterator<String>>(){

			@Override
			public Iterator<String> answer(InvocationOnMock invocation) throws Throwable {
				return TEST_LIST.iterator();
			}
			
		});
		
	}
	
	@Test
	public void givenCachedIterable_whenCallingIteratorTwice_thenOnlyOneIteratorIsCreatedOnDelegateInstance(){
		iterateAndConfirmValues(); 
		iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
	}
	
	@Test
	public void givenCachedIterable_whenCallingEquals_thenReturnsTrue(){
 	 	assertThat(candidate, is(equalTo(new CachedIterable<String>(TEST_LIST))));
 	 	verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	 	iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
 		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	}

	@Test
	public void givenCachedIterableAndEmptyList_whenCallingEquals_thenReturnsFalse(){
		assertThat(candidate, is(not(equalTo(new CachedIterable<String>(Collections.<String>emptyList())))));
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
		iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
	}
	
	@Test
	public void givenCachedIterableAndDifferentIterable_whenCallingEquals_thenReturnsFalse(){
        assertThat(candidate, is(not(equalTo(new CachedIterable<String>(TEST_LIST_DIFFERENT)))));
        verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
        iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
        verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	}

	@Test
	public void givenCachedIterableAndDifferentLengthIterable_whenCallingEquals_thenReturnsFalse(){
 		assertThat(candidate, is(not(equalTo(new CachedIterable<String>(TEST_LIST_DIFFERENT_LENGTH)))));
 	 	verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	 	iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
 	 	verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	}

	@Test
	public void givenAlreadyCachedIterable_whenCallingEquals_thenReturnsTrue(){
 		iterateAndConfirmValues(); // fills cache
 	 	assertThat(candidate, is(not(equalTo(new CachedIterable<String>(TEST_LIST_DIFFERENT_LENGTH))))); // equals still holds
 	 	verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	 	iterateAndConfirmValues(); // iterate twice. This should only iterate on the cached values
 	 	verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
 	}

	@Test
	public void givenNonCachedIterable_whenCallingToArray_thenReturnsCorrectArray(){
		assertThat(Arrays.equals(candidate.toArray(), TEST_LIST.toArray()), is(equalTo(true)));
	}

	@Test
        public void givenCachedIterable_whenCallingToArray_thenReturnsCorrectArray(){
                iterateAndConfirmValues();
		assertThat(Arrays.equals(candidate.toArray(), TEST_LIST.toArray()), is(equalTo(true)));
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
        }

	@Test
	public void givenNonCachedIterable_whenCallingToArrayWithParam_thenReturnsCorrectArray(){
		assertThat(Arrays.equals(candidate.toArray(new String[]{}), TEST_LIST.toArray(new String[]{})), is(equalTo(true)));
	}
	
	@Test
	public void givenCachedIterable_whenCallingToArrayWithParam_thenReturnsCorrectArray(){
		iterateAndConfirmValues();
		assertThat(Arrays.equals(candidate.toArray(new String[]{}), TEST_LIST.toArray(new String[]{})), is(equalTo(true)));
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
	}
	
	@Test
	public void givenCachedIterable_whenCallingIteratorAndRestartingMidWay_thenIteratorIsNotCached(){
		
		Iterator<String> it = candidate.iterator();
		for (int i = 0; i < TEST_LIST.size()/2; i++){
			if (it.hasNext()){
				it.next();
			} else{
				throw new AssertionError("Should never get here!");
			}
		}		
		iterateAndConfirmValues(); 
		
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once. Event if we reset mid way the underlying iterator should still be stored.
	}
	
	@Test
	public void givenCachedIterable_whenCallingIteratorAndThrowingErrorMidWay_thenIteratorIsNotCached(){
		
		Iterable<String> iterationAwareIterable = mock(Iterable.class);
		CachedIterable<String> candidate = new CachedIterable<String>(iterationAwareIterable);

		
		when(iterationAwareIterable.iterator()).then(new Answer<Iterator<String>>(){

			@Override
			public Iterator<String> answer(InvocationOnMock invocation) throws Throwable {
				final Iterator<String> delegate = TEST_LIST.iterator();
				return new Iterator<String>(){

					private int idx;
					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public String next() {
						if (idx++ > (TEST_LIST.size() /2)){
							throw new RuntimeException();
						}
						return delegate.next();
					}

					@Override
					public void remove() {
						// NO OP
					}
					
				};
			}
			
		});
		
		try{
			Iterator<String> it = candidate.iterator();
			while(it.hasNext()){
				it.next();
			}
			fail("No exception was thrown");
		} catch (Exception e){
			// we expect to get here.
		}
		
		when(iterationAwareIterable.iterator()).thenReturn(TEST_LIST.iterator());
		verify(iterationAwareIterable, times(1)).iterator(); // verify that it was only called once
	}
	
	private void iterateAndConfirmValues(){
		int idx = 0;
		for (String item: candidate){
			assertThat(item, is(equalTo(TEST_LIST.get(idx++))));
		}
	}
}
