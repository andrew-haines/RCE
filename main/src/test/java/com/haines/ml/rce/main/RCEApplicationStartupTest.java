package com.haines.ml.rce.main;

import org.junit.Before;
import org.junit.Test;

public class RCEApplicationStartupTest {

	private RCEApplication candidate;
	
	@Before
	public void before() throws RCEApplicationException{
		candidate = new RCEApplication.RCEApplicationBuilder(null).build();
	}
	
	@Test
	public void givenCandidate_whenCallingStart_thenApplicationStartsUpCorrectly() throws RCEApplicationException{
		 candidate.start();
	}
}
