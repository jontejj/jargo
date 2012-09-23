package se.j4j.numbers;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.valueOf;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.numbers.NumberType.INTEGER;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class NumberTypeTest
{
	@Test
	public void testDecimalInput()
	{
		assertThat(INTEGER.decode("112")).isEqualTo(112);
	}

	@Test
	public void testOctalInput()
	{
		assertThat(INTEGER.decode("0112")).isEqualTo(0112);
	}

	@Test
	public void testHexInput()
	{
		assertThat(INTEGER.decode("0x112")).isEqualTo(0x112);
	}

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

	@Test
	public void testByteWithDifferentRadixes()
	{
		testWithDifferentRadixes(NumberType.BYTE);
	}

	@Test
	public void testShortWithDifferentRadixes()
	{
		testWithDifferentRadixes(NumberType.SHORT);
	}

	@Test
	public void testIntegerWithDifferentRadixes()
	{
		testWithDifferentRadixes(NumberType.INTEGER);
	}

	@Test
	public void testLongWithDifferentRadixes()
	{
		testWithDifferentRadixes(NumberType.LONG);
	}

	/**
	 * Tests that {@code type} handles different radixes
	 */
	public <T extends Number> void testWithDifferentRadixes(NumberType<T> type)
	{
		// Decimal numbers
		assertThat(type.decode("0").intValue()).isZero();
		assertThat(type.decode("1").intValue()).isEqualTo(1);
		assertThat(type.decode("-1").intValue()).isEqualTo(-1);

		// Hex numbers
		assertThat(type.decode("0x0").intValue()).isEqualTo(0x0);
		assertThat(type.decode("0x01").intValue()).isEqualTo(0x01);
		assertThat(type.decode("-0x01").intValue()).isEqualTo(-0x01);

		// Octal numbers
		assertThat(type.decode("01").intValue()).isEqualTo(001);
		assertThat(type.decode("-01").intValue()).isEqualTo(-001);

		// Numbers at the bounds
		assertThat(type.decode(type.minValue().toString())).isEqualTo(type.minValue());
		assertThat(type.decode(type.maxValue().toString())).isEqualTo(type.maxValue());

		// Default values
		assertThat(type.defaultValue().intValue()).isZero();
	}

	@Test
	public void testThatToStringReturnsName()
	{
		for(NumberType<?> type : NumberType.TYPES)
		{
			assertThat(type.toString()).isEqualTo(type.name());
		}
	}

	@Test
	public void testInvalidShortNumbers()
	{
		List<Integer> invalidInput = Arrays.asList(Short.MIN_VALUE - 1, Short.MAX_VALUE + 1);
		for(Integer input : invalidInput)
		{
			try
			{
				NumberType.SHORT.decode(input.toString());
				fail("Invalid short input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{

			}
		}
	}

	@Test
	public void testInvalidIntegerNumbers()
	{
		List<Long> invalidInput = Arrays.asList((long) Integer.MIN_VALUE - 1, (long) Integer.MAX_VALUE + 1);
		for(Long input : invalidInput)
		{
			try
			{
				NumberType.INTEGER.decode(input.toString());
				fail("Invalid integer input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{

			}
		}
	}

	@Test
	public void testInvalidLongNumbers()
	{
		List<BigInteger> invalidInput = Arrays.asList(valueOf(MIN_VALUE).subtract(ONE), valueOf(MAX_VALUE).add(ONE));
		for(BigInteger input : invalidInput)
		{
			try
			{
				NumberType.LONG.decode(input.toString());
				fail("Invalid long input not detected: " + input);
			}
			catch(IllegalArgumentException expected)
			{

			}
		}
	}
}
