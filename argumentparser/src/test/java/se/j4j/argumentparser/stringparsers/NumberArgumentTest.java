package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.StringParsers.integerParser;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;
import static se.j4j.numbers.NumberType.INTEGER;
import static se.j4j.strings.StringsUtil.NEWLINE;

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
		String validIntegers = "<" + INTEGER.name() + ">: " + INTEGER.minValue() + " to " + INTEGER.maxValue() + NEWLINE;
		String usage = withParser(integerParser()).usage("");
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
}
