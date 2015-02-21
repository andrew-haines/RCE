package com.haines.ml.rce.model.distribution;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class DistributionParametersUnitTest {
	
	private static final DistributionParameters UNIT_PARAMETERS = new DistributionParameters(100, 0, 1);
	private static final DistributionParameters UNIT_PARAMETERS_SHIFTED = new DistributionParameters(100, 1, 1);
	
	@Test
	public void givenUnitParameters_whenCallingAddWithAnotherUnitParameters_thenValuesAreTheSame(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(UNIT_PARAMETERS, UNIT_PARAMETERS);
		
		assertThat(summedParams.getMean(), is(equalTo(0.0)));
		assertThat(summedParams.getVariance(), is(equalTo(1.0)));
		assertThat(summedParams.getNumSamples(), is(equalTo(200)));
	}
	
	@Test
	public void givenUnitParameters_whenCallingAddWithUnitParametersShifted_thenResultShiftedAppropriately(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(UNIT_PARAMETERS, UNIT_PARAMETERS_SHIFTED);
		
		assertThat(summedParams.getMean(), is(equalTo(0.5)));
		assertThat(summedParams.getVariance(), is(equalTo(1.25)));
		assertThat(summedParams.getNumSamples(), is(equalTo(200)));
	}
	
	@Test
	public void givenShiftedUnitParameters_whenCallingSubWithUnitParameters_thenResultUnitParametersShifted(){
		DistributionParameters summedParams = DistributionParameters.MATHS.sub(new DistributionParameters(200, 0.5, 1.25), UNIT_PARAMETERS);
		
		assertThat(summedParams.getMean(), is(equalTo(1.0)));
		assertThat(summedParams.getVariance(), is(equalTo(1.00)));
		assertThat(summedParams.getNumSamples(), is(equalTo(100)));
	}
}
