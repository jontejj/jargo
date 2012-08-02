package se.j4j.argumentparser.stringparsers;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.math.BigInteger.ONE;
import static java.math.BigInteger.valueOf;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.longArgument;
import static se.j4j.argumentparser.ArgumentFactory.shortArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.StringParsers.byteParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.longParser;
import static se.j4j.argumentparser.StringParsers.shortParser;
import static se.j4j.argumentparser.internal.Platform.NEWLINE;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;
import se.j4j.argumentparser.internal.NumberType;

public class NumberArgumentTest
{
	@Test
	public void testDecimalInput() throws ArgumentException
	{
		assertThat(integerArgument().parse("112")).isEqualTo(112);
	}

	@Test
	public void testOctalInput() throws ArgumentException
	{
		assertThat(integerArgument().parse("0112")).isEqualTo(0112);
	}

	@Test
	public void testHexInput() throws ArgumentException
	{
		assertThat(integerArgument().parse("0x112")).isEqualTo(0x112);
	}

	@Test
	public void testByteWithDifferentRadixes() throws ArgumentException
	{
		testWithDifferentRadixes(byteParser(), NumberType.BYTE);
	}

	@Test
	public void testShortWithDifferentRadixes() throws ArgumentException
	{
		testWithDifferentRadixes(shortParser(), NumberType.SHORT);
	}

	@Test
	public void testIntegerWithDifferentRadixes() throws ArgumentException
	{
		testWithDifferentRadixes(integerParser(), NumberType.INTEGER);
	}

	@Test
	public void testLongWithDifferentRadixes() throws ArgumentException
	{
		testWithDifferentRadixes(longParser(), NumberType.LONG);
	}

	/**
	 * Tests that {@code parser} is well integrated with {@code type}
	 */
	public <T extends Number> void testWithDifferentRadixes(StringParser<T> parser, NumberType<T> type) throws ArgumentException
	{
		// Decimal numbers
		assertThat(parser.parse("0").intValue()).isZero();
		assertThat(parser.parse("1").intValue()).isEqualTo(1);
		assertThat(parser.parse("-1").intValue()).isEqualTo(-1);

		// Hex numbers
		assertThat(parser.parse("0x0").intValue()).isEqualTo(0x0);
		assertThat(parser.parse("0x01").intValue()).isEqualTo(0x01);
		assertThat(parser.parse("-0x01").intValue()).isEqualTo(-0x01);

		// Octal numbers
		assertThat(parser.parse("01").intValue()).isEqualTo(001);
		assertThat(parser.parse("-01").intValue()).isEqualTo(-001);

		// Numbers at the bounds
		assertThat(parser.parse(type.minValue().toString())).isEqualTo(type.minValue());
		assertThat(parser.parse(type.maxValue().toString())).isEqualTo(type.maxValue());

		// Default values
		assertThat(parser.defaultValue().intValue()).isZero();
		assertThat(withParser(parser).parse().intValue()).isZero();

		// Usage
		String validValuesForType = "<" + type.name() + ">: " + type.minValue() + " to " + type.maxValue() + NEWLINE;
		String usage = withParser(parser).usage("");
		assertThat(usage).contains(validValuesForType);
		assertThat(usage).contains("Default: 0" + NEWLINE);
	}

	@Test
	public void testInvalidByteArguments()
	{
		List<Integer> invalidInput = Arrays.asList(Byte.MIN_VALUE - 1, Byte.MAX_VALUE + 1);
		for(Integer input : invalidInput)
		{
			try
			{
				byteArgument("-b").parse("-b", input.toString());
				fail("Invalid byte input not detected: " + input);
			}
			catch(ArgumentException e)
			{
				assertThat(e.getMessageAndUsage("InvalidByte")).isEqualTo(expected("InvalidByte" + input));
			}
		}

		try
		{
			byteArgument("-b").parse("-b", "NaN");
			fail("Not a number not detected");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessageAndUsage("NaNTest")).isEqualTo(expected("ByteNaN"));
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
				shortArgument("-b").parse("-b", input.toString());
				fail("Invalid short input not detected: " + input);
			}
			catch(ArgumentException expected)
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
				integerArgument("-b").parse("-b", input.toString());
				fail("Invalid integer input not detected: " + input);
			}
			catch(ArgumentException expected)
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
				longArgument("-b").parse("-b", input.toString());
				fail("Invalid long input not detected: " + input);
			}
			catch(ArgumentException expected)
			{

			}
		}
	}
}
