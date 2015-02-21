package com.haines.ml.rce.model.distribution;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class DistirbutionUnitTest {
	
	private static final DistributionParameters UNIT_PARAMTERS = new DistributionParameters(10, 0, 1);
	private static final DistributionParameters UNIT_PARAMTERS_SHIFTED = new DistributionParameters(10, 1, 1);
	private static final DistributionParameters NOMINAL_PARAMTERS = new DistributionParameters(10, 0, 0.0000000001);
	private static final DistributionParameters REAL_PARAMTERS = new DistributionParameters(10, 25.6554, 816.644929);

	private Distribution normalDistributionCandidate;
	
	@Before
	public void before(){
		normalDistributionCandidate = Distribution.NORMAL_DISTRIBUTION;
	}
	
	@Test
	public void givenNormalDistributionCandidate_whenCallingGetValueWithUnitParameters_thenExpectedResultReturned(){
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, 0), is(equalTo(0.3989422804014327)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, 1), is(equalTo(0.24197072451914337)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, 0.5), is(equalTo(0.3520653267642995)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, 1.25), is(equalTo(0.18264908538902191)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, -1), is(equalTo(0.24197072451914337)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, -0.5), is(equalTo(0.3520653267642995)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, -1.25), is(equalTo(0.18264908538902191)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, -100), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS, 100), is(equalTo(0.0)));
	}
	
	@Test
	public void givenNormalDistributionCandidate_whenCallingGetValueWithUnitParametersShifted_thenExpectedResultReturned(){
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, 0), is(equalTo(0.24197072451914337)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, 1), is(equalTo(0.3989422804014327)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, 0.5), is(equalTo(0.3520653267642995)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, 1.25), is(equalTo(0.38666811680284924)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, -1), is(equalTo(0.05399096651318806)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, -0.5), is(equalTo(0.12951759566589174)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, -1.25), is(equalTo(0.03173965183566742)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, -100), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(UNIT_PARAMTERS_SHIFTED, 100), is(equalTo(0.0)));
	}
	
	@Test
	public void givenNormalDistributionCandidate_whenCallingGetValueWithZeroParameters_thenExpectedResultReturned(){
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, 0), is(equalTo(39894.228040143265)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, 1), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, 0.5), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, 1.25), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, -1), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, -0.5), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, -1.25), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, -100), is(equalTo(0.0)));
		assertThat(normalDistributionCandidate.getValue(NOMINAL_PARAMTERS, 100), is(equalTo(0.0)));
	}
	
	@Test
	public void givenNormalDistributionCandidate_whenCallingGetValueWithRealParameters_thenExpectedResultReturned(){
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 0), is(equalTo(0.009329901942357643)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 1), is(equalTo(0.009621766304849386)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 0.5), is(equalTo(0.009476160706151406)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 1.25), is(equalTo(0.009694292995694041)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, -1), is(equalTo(0.009035819583215454)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, -0.5), is(equalTo(0.009183088938575293)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, -1.25), is(equalTo(0.008962044055037702)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, -100), is(equalTo(0.0000008840927187716609)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 100), is(equalTo(0.0004733944659551913)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 25.6554), is(equalTo(0.013960257563825197)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 20), is(equalTo(0.01368954306239625)));
		assertThat(normalDistributionCandidate.getValue(REAL_PARAMTERS, 30), is(equalTo(0.013799850806928289)));
	}
}
