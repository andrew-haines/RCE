package com.haines.ml.rce.model.distribution;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class DistributionParametersUnitTest {
	
	private static final DistributionParameters UNIT_PARAMETERS = new DistributionParameters(100, 0, 1);
	private static final DistributionParameters UNIT_PARAMETERS_SHIFTED = new DistributionParameters(100, 1, 1);
	
	@Test
	public void givenUnitParameters_whenCallingAddWithAnotherUnitParameters_thenValuesAreTheSame(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(UNIT_PARAMETERS, UNIT_PARAMETERS);
		
		assertThat(summedParams.getMean(), is(equalTo(0.0)));
		assertThat(summedParams.getVariance(), is(equalTo(0.9949748743718593)));
		assertThat(summedParams.getNumSamples(), is(equalTo(200)));
	}
	
	@Test
	public void givenUnitParameters_whenCallingAddWithUnitParametersShifted_thenResultShiftedAppropriately(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(UNIT_PARAMETERS, UNIT_PARAMETERS_SHIFTED);
		
		assertThat(summedParams.getMean(), is(equalTo(0.5)));
		assertThat(summedParams.getVariance(), is(equalTo(1.2462311557788945)));
		assertThat(summedParams.getNumSamples(), is(equalTo(200)));
	}
	
	@Test
	public void givenShiftedUnitParameters_whenCallingSubWithUnitParameters_thenResultUnitParametersShifted(){
		DistributionParameters summedParams = DistributionParameters.MATHS.sub(new DistributionParameters(200, 0.5, 1.25), UNIT_PARAMETERS);
		
		assertThat(summedParams.getMean(), is(equalTo(1.0)));
		assertThat(summedParams.getVariance(), is(equalTo(1.0075757575757576)));
		assertThat(summedParams.getNumSamples(), is(equalTo(100)));
	}
	
	@Test
	public void givenSingleEventWithNaNVarianceParameters_whenCallingAddWithAnotherSingleNaNVarianceEventParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(new DistributionParameters(1, 13, Double.NaN), new DistributionParameters(1, 15, Double.NaN));
		
		assertThat(summedParams.getMean(), is(equalTo(14.0)));
		assertThat(summedParams.getVariance(), is(equalTo(2.00)));
		assertThat(summedParams.getNumSamples(), is(equalTo(2)));
	}
	
	@Test
	public void givenVarianceParameters_whenCallingAddWithSingleNaNVarianceEventParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(new DistributionParameters(2, 14, 2), new DistributionParameters(1, 18, Double.NaN));
		
		assertThat(summedParams.getMean(), is(equalTo(15.333333333333334)));
		assertThat(summedParams.getVariance(), is(equalTo(6.333333333333334)));
		assertThat(summedParams.getNumSamples(), is(equalTo(3)));
	}
	
	@Test
	public void givenVarianceOf3EventParameters_whenCallingAddWithSingleNaNVarianceEventParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(new DistributionParameters(3, 15.333333333333334, 6.333333333333334), new DistributionParameters(1, 10, Double.NaN));
		
		assertThat(summedParams.getMean(), is(equalTo(14.0)));
		assertThat(summedParams.getVariance(), is(equalTo(11.333333333333336)));
		assertThat(summedParams.getNumSamples(), is(equalTo(4)));
	}
	
	@Test
	public void givenVarianceOf4EventParameters_whenCallingAddWithVarianceEventParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(new DistributionParameters(4, 14.0, 11.333333333333336), new DistributionParameters(2, 48.5, 544.5));
		
		assertThat(summedParams.getMean(), is(equalTo(25.5)));
		assertThat(summedParams.getVariance(), is(equalTo(433.1)));
		assertThat(summedParams.getNumSamples(), is(equalTo(6)));
	}
	
	@Test
	public void given1SampleDistribution_whenCallingAdd1SampleDistributionParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.add(new DistributionParameters(1, 57.6, Double.NaN), new DistributionParameters(1, 57.3, Double.NaN));
		
		assertThat(summedParams.getMean(), is(equalTo(57.45)));
		assertThat(summedParams.getVariance(), is(equalTo(0.045000000000001275)));
		assertThat(summedParams.getNumSamples(), is(equalTo(2)));
	}
	
	@Test
	public void givenSingleEventVarianceOf6EventParameters_whenCallingSubWithVarianceEventParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.sub(new DistributionParameters(6, 25.5, 433.1), new DistributionParameters(2, 48.5, 544.5));
		
		assertThat(summedParams.getMean(), is(equalTo(14.0)));
		assertThat(summedParams.getVariance(), is(equalTo(11.333333333333334)));
		assertThat(summedParams.getNumSamples(), is(equalTo(4)));
		
		// belts and braces check that the equals method works
		assertThat(summedParams, is(equalTo(new DistributionParameters(4, 14.0, 11.333333333333334))));
	}
	
	@Test
	public void givenSingleEventVarianceOf4EventParameters_whenCallingSubWithNaNVarianceEventParameter_thenResultDoesNotHaveNaNVariance(){
		DistributionParameters summedParams = DistributionParameters.MATHS.sub(new DistributionParameters(4, 14.0, 11.333333333333336), new DistributionParameters(1, 10, Double.NaN));
		
		assertThat(summedParams.getMean(), is(equalTo(15.333333333333334)));
		assertThat(summedParams.getVariance(), is(equalTo(6.333333333333334)));
		assertThat(summedParams.getNumSamples(), is(equalTo(3)));
		
		assertThat(summedParams, is(equalTo(new DistributionParameters(3, 15.333333333333334, 6.333333333333334))));
	}
	
	@Test
	public void givenDistribution_whenCallingEqualsToOnDifferentParameters_thenReturnsFalse(){
		assertThat(new DistributionParameters(4, 14.0, 11.333333333333336), is(not(equalTo(new DistributionParameters(6, 77, 11.333333333333336)))));
	}
}