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
import static se.j4j.argumentparser.ArgumentFactory.Radix.BINARY;
import static se.j4j.argumentparser.ArgumentFactory.Radix.DECIMAL;
import static se.j4j.argumentparser.ArgumentFactory.Radix.HEX;
import static se.j4j.argumentparser.ArgumentFactory.Radix.OCTAL;
import static se.j4j.argumentparser.ArgumentFactory.Radix.UnsignedOutput.NO;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentFactory.ByteArgument;
import se.j4j.argumentparser.ArgumentFactory.IntegerArgument;
import se.j4j.argumentparser.ArgumentFactory.LongArgument;
import se.j4j.argumentparser.ArgumentFactory.Radix;
import se.j4j.argumentparser.ArgumentFactory.Radix.UnsignedOutput;
import se.j4j.argumentparser.ArgumentFactory.RadixiableArgument;
import se.j4j.argumentparser.ArgumentFactory.ShortArgument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.internal.Lines;
import se.j4j.argumentparser.utils.UsageTexts;

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
		assertThat(binary).isEqualTo(9);
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
			ByteArgument handler = new ByteArgument(radix);

			for(Byte value : numbersToTest(handler))
			{
				String describedValue = handler.describeValue(value);
				Byte parsedValue = handler.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = byteArgument("-b").defaultValue((byte) 32).radix(radix).usage("");
			assertThat(usage).contains("<byte>: " + validInputs.get(radix) + Lines.NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + Lines.NEWLINE);
		}
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
				String expectedUsage = UsageTexts.get("InvalidByte" + input + ".txt");
				assertThat(e.getMessageAndUsage("InvalidByte")).isEqualTo(expectedUsage);
			}
		}

		try
		{
			byteArgument("-b").parse("-b", "NaN");
			fail("Not a number not detected");
		}
		catch(InvalidArgument e)
		{
			String expectedUsage = UsageTexts.get("ByteNaN.txt");
			assertThat(e.getMessageAndUsage("NaNTest")).isEqualTo(expectedUsage);
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
			ShortArgument handler = new ShortArgument(radix);
			for(Short value : numbersToTest(handler))
			{
				String describedValue = handler.describeValue(value);
				Short parsedValue = handler.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = shortArgument("-b").defaultValue((short) 32).radix(radix).usage("");
			assertThat(usage).contains("<short>: " + validInputs.get(radix) + Lines.NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + Lines.NEWLINE);
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
			IntegerArgument handler = new IntegerArgument(radix);
			for(Integer value : numbersToTest(handler))
			{
				String describedValue = handler.describeValue(value);
				Integer parsedValue = handler.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = integerArgument("-b").defaultValue(32).radix(radix).usage("");
			assertThat(usage).contains("<integer>: " + validInputs.get(radix) + Lines.NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + Lines.NEWLINE);
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

	public static <T extends Number & Comparable<T>> Iterable<T> numbersToTest(RadixiableArgument<T> arg)
	{
		T minValueT = arg.minValue();
		long minValue = arg.cast(minValueT);

		T maxValueT = arg.maxValue();
		long maxValue = arg.cast(maxValueT);

		double stepSize = (double) 5 / 100;

		long rangeLength = maxValue - minValue;
		List<T> numbers = Lists.newArrayListWithExpectedSize(22);

		for(long i = (long) (minValue + rangeLength * stepSize); i < maxValue; i = (long) (i + rangeLength * stepSize))
		{
			numbers.add(arg.cast(i));
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
			LongArgument handler = new LongArgument(radix);
			for(Long value : longNumbers())
			{
				String describedValue = handler.describeValue(value);
				Long parsedValue = handler.parse(describedValue);
				assertThat(parsedValue).isEqualTo(value);
			}
			String usage = longArgument("-b").defaultValue(32L).radix(radix).usage("");
			assertThat(usage).contains("<long>: " + validInputs.get(radix) + Lines.NEWLINE);
			assertThat(usage).contains("Default: " + defaultValues.get(radix) + Lines.NEWLINE);
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

	private static Iterable<Long> longNumbers()
	{
		long stepSize = (long) (Long.MAX_VALUE * (double) 5 / 100);

		List<Long> numbers = Lists.newArrayListWithExpectedSize(100);

		long end = Long.MAX_VALUE - stepSize;
		for(long i = Long.MIN_VALUE + stepSize; i < end; i += stepSize)
		{
			numbers.add(i);
		}
		numbers.add(Long.MAX_VALUE);
		numbers.add(Long.MIN_VALUE);

		return numbers;
	}
}
