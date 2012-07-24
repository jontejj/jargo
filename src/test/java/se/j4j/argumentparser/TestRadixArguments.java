package se.j4j.argumentparser;

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
import static se.j4j.argumentparser.StringParsers.byteParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.StringParsers.longParser;
import static se.j4j.argumentparser.StringParsers.shortParser;
import static se.j4j.argumentparser.StringParsers.Radix.BINARY;
import static se.j4j.argumentparser.StringParsers.Radix.DECIMAL;
import static se.j4j.argumentparser.StringParsers.Radix.HEX;
import static se.j4j.argumentparser.StringParsers.Radix.OCTAL;
import static se.j4j.argumentparser.StringParsers.Radix.UnsignedOutput.NO;
import static se.j4j.argumentparser.internal.Platform.NEWLINE;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.Radix;
import se.j4j.argumentparser.StringParsers.Radix.UnsignedOutput;
import se.j4j.argumentparser.StringParsers.RadixiableParser;
import se.j4j.argumentparser.internal.NumberType;
import se.j4j.argumentparser.internal.OneBitType;

import com.google.common.collect.Lists;

public class TestRadixArguments
{
	@Test
	public void testOctalInput() throws ArgumentException
	{
		String input = Integer.toString(0112, OCTAL.radix());
		Integer octal = integerArgument("-o").radix(OCTAL).parse("-o", input);
		assertThat(octal).isEqualTo(0112);
	}

	@Test
	public void testHexInput() throws ArgumentException
	{
		Integer hex = integerArgument("-h").radix(HEX).parse("-h", "FF");
		assertThat(hex).isEqualTo(0xFF);
	}

	@Test
	public void testBinaryInput() throws ArgumentException
	{
		Integer binary = integerArgument("-b").radix(BINARY).parse("-b", "1001");
		assertThat(binary).isEqualTo(0b1001);
	}

	@Test
	public void testUnsignedOutputEnum()
	{
		assertThat(UnsignedOutput.valueOf(NO.toString())).isEqualTo(NO);
	}

	@Test
	public void testByteWithDifferentRadixes() throws ArgumentException
	{
		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> validInputs = new HashMap<Radix, String>(){
			{
				put(BINARY, "00000000 to 11111111 (binary)");
				put(OCTAL, "000 to 377 (octal)");
				put(DECIMAL, "-128 to 127 (decimal)");
				put(HEX, "00 to FF (hex)");
			}
		};

		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> defaultValues = new HashMap<Radix, String>(){
			{
				put(BINARY, "00100000");
				put(OCTAL, "40");
				put(DECIMAL, "32");
				put(HEX, "20");
			}
		};

		Radix[] test = new Radix[]{Radix.BINARY};
		for(Radix radix : test)
		{
			RadixiableParser<Byte> parser = (RadixiableParser<Byte>) byteParser(radix);

			for(Byte value : numbersToTest(NumberType.BYTE))
			{
				String describedValue = parser.describeValue(value);
				Byte parsedValue = parser.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = byteArgument("-b").defaultValue((byte) 32).radix(radix).usage("");
			assertThat(usage).contains("<byte>: " + validInputs.get(radix) + NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + NEWLINE);
		}
	}

	@Test
	public void testOutOfRangeInHex() throws ArgumentException
	{
		// TODO: what should the defaultValue be here?
		Argument<Integer> hexArgument = integerArgument().radix(HEX).minValue(0x42).maxValue(0x60).build();

		CommandLineParser parser = CommandLineParser.forArguments(hexArgument);
		try
		{
			parser.parse("0F");
			fail("0x0F shouldn't be valid since it's lower than 0x42");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected.getMessageAndUsage("OutOfRangeInHex")).isEqualTo(expected("outOfRangeInHex"));
		}
		ParsedArguments parsed = parser.parse("42");
		assertThat(parsed.get(hexArgument)).isEqualTo(0x42);

		assertThat(integerArgument().minValue(1).maxValue(3).parse("2")).isEqualTo(2);
	}

	@Test
	public void testInvalidByteArguments() throws ArgumentException
	{
		List<Integer> invalidInput = Arrays.asList(Byte.MIN_VALUE - 1, Byte.MAX_VALUE + 1);
		for(Integer input : invalidInput)
		{
			try
			{
				byteArgument("-b").parse("-b", input.toString());
				fail("Invalid byte input not detected: " + input);
			}
			catch(InvalidArgument e)
			{
				assertThat(e.getMessageAndUsage("InvalidByte")).isEqualTo(expected("InvalidByte" + input));
			}
		}

		try
		{
			byteArgument("-b").parse("-b", "NaN");
			fail("Not a number not detected");
		}
		catch(InvalidArgument e)
		{
			assertThat(e.getMessageAndUsage("NaNTest")).isEqualTo(expected("ByteNaN"));
		}
	}

	@Test
	public void testShortWithDifferentRadixes() throws ArgumentException
	{
		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> validInputs = new HashMap<Radix, String>(){
			{
				put(BINARY, "0000000000000000 to 1111111111111111 (binary)");
				put(OCTAL, "000000 to 177777 (octal)");
				put(DECIMAL, "-32768 to 32767 (decimal)");
				put(HEX, "0000 to FFFF (hex)");
			}
		};

		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> defaultValues = new HashMap<Radix, String>(){
			{
				put(BINARY, "0000000000100000");
				put(OCTAL, "40");
				put(DECIMAL, "32");
				put(HEX, "20");
			}
		};

		for(Radix radix : Radix.values())
		{
			RadixiableParser<Short> parser = (RadixiableParser<Short>) shortParser(radix);
			for(Short value : numbersToTest(NumberType.SHORT))
			{
				String describedValue = parser.describeValue(value);
				Short parsedValue = parser.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = shortArgument("-b").defaultValue((short) 32).radix(radix).usage("");
			assertThat(usage).contains("<short>: " + validInputs.get(radix) + NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + NEWLINE);
		}
	}

	@Test
	public void testInvalidShortNumbers() throws ArgumentException
	{
		List<Integer> invalidInput = Arrays.asList(Short.MIN_VALUE - 1, Short.MAX_VALUE + 1);
		for(Integer input : invalidInput)
		{
			try
			{
				shortArgument("-b").parse("-b", input.toString());
				fail("Invalid short input not detected: " + input);
			}
			catch(InvalidArgument expected)
			{

			}
		}
	}

	@Test
	public void testIntegerWithDifferentRadixes() throws ArgumentException
	{
		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> validInputs = new HashMap<Radix, String>(){
			{
				put(BINARY, "00000000000000000000000000000000 to 11111111111111111111111111111111 (binary)");
				put(OCTAL, "00000000000 to 37777777777 (octal)");
				put(DECIMAL, "-2147483648 to 2147483647 (decimal)");
				put(HEX, "00000000 to FFFFFFFF (hex)");
			}
		};

		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> defaultValues = new HashMap<Radix, String>(){
			{
				put(BINARY, "00000000000000000000000000100000");
				put(OCTAL, "40");
				put(DECIMAL, "32");
				put(HEX, "20");
			}
		};

		for(Radix radix : Radix.values())
		{
			RadixiableParser<Integer> parser = (RadixiableParser<Integer>) integerParser(radix);
			for(Integer value : numbersToTest(NumberType.INTEGER))
			{
				String describedValue = parser.describeValue(value);
				Integer parsedValue = parser.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = integerArgument("-b").defaultValue(32).radix(radix).usage("");
			assertThat(usage).contains("<integer>: " + validInputs.get(radix) + NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + NEWLINE);
		}
	}

	@Test
	public void testInvalidIntegerNumbers() throws ArgumentException
	{
		List<Long> invalidInput = Arrays.asList((long) Integer.MIN_VALUE - 1, (long) Integer.MAX_VALUE + 1);
		for(Long input : invalidInput)
		{
			try
			{
				integerArgument("-b").parse("-b", input.toString());
				fail("Invalid integer input not detected: " + input);
			}
			catch(InvalidArgument expected)
			{

			}
		}
	}

	@Test
	public void testByteDefaultValue() throws ArgumentException
	{
		assertThat(byteArgument("-b").parse()).isZero();
	}

	@Test
	public void testShortDefaultValue() throws ArgumentException
	{
		assertThat(shortArgument("-b").parse()).isZero();
	}

	@Test
	public void testIntegerDefaultValue() throws ArgumentException
	{
		assertThat(integerArgument("-b").parse()).isZero();
	}

	@Test
	public void testLongDefaultValue() throws ArgumentException
	{
		assertThat(longArgument("-b").parse()).isZero();
	}

	/**
	 * Generates some good numbers to test for the given <code>type</code>
	 */
	public static <T extends Number & Comparable<T>> Iterable<T> numbersToTest(NumberType<T> type)
	{
		T minValueT = type.minValue();
		long minValue = type.toLong(minValueT);

		T maxValueT = type.maxValue();
		long maxValue = type.toLong(maxValueT);

		double stepSize = (double) 5 / 100;

		long rangeLength = maxValue - minValue;
		List<T> numbers = Lists.newArrayListWithExpectedSize(22);

		for(long i = (long) (minValue + rangeLength * stepSize); i < maxValue; i = (long) (i + rangeLength * stepSize))
		{
			numbers.add(type.fromLong(i));
		}
		numbers.add(maxValueT);
		numbers.add(minValueT);

		return numbers;
	}

	@Test
	public void testLongWithDifferentRadixes() throws ArgumentException
	{
		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> validInputs = new HashMap<Radix, String>(){
			{
				put(BINARY,
					"0000000000000000000000000000000000000000000000000000000000000000 to 1111111111111111111111111111111111111111111111111111111111111111 (binary)");
				put(OCTAL, "0000000000000000000000 to 1777777777777777777777 (octal)");
				put(DECIMAL, "-9223372036854775808 to 9223372036854775807 (decimal)");
				put(HEX, "0000000000000000 to FFFFFFFFFFFFFFFF (hex)");
			}
		};

		// Only used as test assertion
		@SuppressWarnings("serial")
		Map<Radix, String> defaultValues = new HashMap<Radix, String>(){
			{
				put(BINARY, "0000000000000000000000000000000000000000000000000000000000100000");
				put(OCTAL, "40");
				put(DECIMAL, "32");
				put(HEX, "20");
			}
		};

		for(Radix radix : Radix.values())
		{
			RadixiableParser<Long> parser = (RadixiableParser<Long>) longParser(radix);
			for(Long value : longNumbers())
			{
				String describedValue = parser.describeValue(value);
				Long parsedValue = parser.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = longArgument("-b").defaultValue(32L).radix(radix).usage("");
			assertThat(usage).contains("<long>: " + validInputs.get(radix) + NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + NEWLINE);
		}
	}

	@Test
	public void testInvalidLongNumbers() throws ArgumentException
	{
		List<BigInteger> invalidInput = Arrays.asList(valueOf(MIN_VALUE).subtract(ONE), valueOf(MAX_VALUE).add(ONE));
		for(BigInteger input : invalidInput)
		{
			try
			{
				longArgument("-b").parse("-b", input.toString());
				fail("Invalid long input not detected: " + input);
			}
			catch(InvalidArgument expected)
			{

			}
		}
	}

	/**
	 * Generates some good long numbers to test
	 */
	private static Iterable<Long> longNumbers()
	{
		long stepSize = (long) (Long.MAX_VALUE * (double) 5 / 100);

		List<Long> numbers = Lists.newArrayListWithExpectedSize(22);

		long end = Long.MAX_VALUE - stepSize;
		for(long i = Long.MIN_VALUE + stepSize; i < end; i += stepSize)
		{
			numbers.add(i);
		}
		numbers.add(Long.MAX_VALUE);
		numbers.add(Long.MIN_VALUE);

		return numbers;
	}

	@Test
	public void testOneBitNumber()
	{
		RadixiableParser<Integer> parser = StringParsers.RadixiableParser.radixiableParser(Radix.DECIMAL, new OneBitType());

		String bitString = parser.describeValue(0);
		assertThat(bitString).isEqualTo("0");

		bitString = parser.describeValue(1);
		assertThat(bitString).isEqualTo("1");
	}
}
