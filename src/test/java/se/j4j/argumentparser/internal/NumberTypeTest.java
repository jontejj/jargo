package se.j4j.argumentparser.internal;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class NumberTypeTest
{
	@Test
	public void testMappingOfByteFields()
	{
		assertThat(NumberType.BYTE.maxValue()).isEqualTo(Byte.MAX_VALUE);
		assertThat(NumberType.BYTE.minValue()).isEqualTo(Byte.MIN_VALUE);
	}

	@Test
	public void testMappingOfShortFields()
	{
		assertThat(NumberType.SHORT.maxValue()).isEqualTo(Short.MAX_VALUE);
		assertThat(NumberType.SHORT.minValue()).isEqualTo(Short.MIN_VALUE);
	}

	@Test
	public void testMappingOfIntegerFields()
	{
		assertThat(NumberType.INTEGER.maxValue()).isEqualTo(Integer.MAX_VALUE);
		assertThat(NumberType.INTEGER.minValue()).isEqualTo(Integer.MIN_VALUE);
	}

	@Test
	public void testMappingOfLongFields()
	{
		assertThat(NumberType.LONG.maxValue()).isEqualTo(Long.MAX_VALUE);
		assertThat(NumberType.LONG.minValue()).isEqualTo(Long.MIN_VALUE);
	}
}
