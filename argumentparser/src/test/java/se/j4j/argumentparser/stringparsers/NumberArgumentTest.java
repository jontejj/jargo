package se.j4j.argumentparser.stringparsers;

import static java.util.Locale.US;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.bigDecimalArgument;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.longArgument;
import static se.j4j.argumentparser.ArgumentFactory.shortArgument;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;
import static se.j4j.strings.StringsUtil.NEWLINE;
import static se.j4j.testlib.Locales.SWEDISH;
import static se.j4j.testlib.Locales.TURKISH;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.StringParsers;

/**
 * <pre>
 * Tests for
 * {@link ArgumentFactory#byteArgument(String...)},
 * {@link ArgumentFactory#shortArgument(String...)},
 * {@link ArgumentFactory#integerArgument(String...)},
 * {@link ArgumentFactory#longArgument(String...)}
 * and
 * {@link StringParsers#byteParser()},
 * {@link StringParsers#shortParser()},
 * {@link StringParsers#integerParser()},
 * {@link StringParsers#longParser()}
 */
public class NumberArgumentTest
{
	@Test
	public void testUsage()
	{
		String validIntegers = "<integer>: -2,147,483,648 to 2,147,483,647" + NEWLINE;
		String usage = integerArgument().locale(US).usage();
		assertThat(usage).contains(validIntegers);
		assertThat(usage).contains("Default: 0" + NEWLINE);
	}

	@Test
	public void testInvalidNumberArguments()
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
				assertThat(e.getMessageAndUsage()).isEqualTo(expected("InvalidByte" + input));
			}
		}

		try
		{
			byteArgument("-b").locale(SWEDISH).parse("-b", "NaN");
			fail("Not a number not detected");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessageAndUsage()).isEqualTo(expected("ByteNaN"));
		}
	}

	@Test
	public void testThatDefaultValueForShortIsFormattedInTheChosenLocale()
	{
		String b = shortArgument().locale(TURKISH).defaultValue(Short.MAX_VALUE).usage();
		assertThat(b).contains("Default: 32.767" + NEWLINE);
	}

	@Test
	public void testThatDefaultValueForIntegerIsFormattedInTheChosenLocale()
	{
		String b = integerArgument().locale(TURKISH).defaultValue(Integer.MAX_VALUE).usage();
		assertThat(b).contains("Default: 2.147.483.647" + NEWLINE);
	}

	@Test
	public void testThatDefaultValueForLongIsFormattedInTheChosenLocale()
	{
		String b = longArgument().locale(TURKISH).defaultValue(Long.MAX_VALUE).usage();
		assertThat(b).contains("Default: 9.223.372.036.854.775.807" + NEWLINE);
	}

	@Test
	public void testThatDefaultValueForBigDecimalIsFormattedInTheChosenLocale()
	{
		String b = bigDecimalArgument().locale(TURKISH).defaultValue(BigDecimal.valueOf(Long.MAX_VALUE)).usage();
		assertThat(b).contains("Default: 9.223.372.036.854.775.807" + NEWLINE);
	}
}
