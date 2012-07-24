package se.j4j.argumentparser;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.MissingRequiredArgumentException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestRequiredArguments
{
	@Test
	public void testMissingRequiredArgument() throws ArgumentException
	{
		Argument<Integer> number = integerArgument("--number").required().build();
		Argument<Integer> number2 = integerArgument("--number2").required().build();

		try
		{
			CommandLineParser.forArguments(number, number2).parse();
			fail("Required argument silently ignored");
		}
		catch(MissingRequiredArgumentException e)
		{
			assertThat(e).hasMessage("Missing required arguments: [--number, --number2]");
		}
	}

	@Test(expected = MissingRequiredArgumentException.class)
	public void testThatRequiredArgumentsIsResetBetweenParsings() throws ArgumentException
	{
		Argument<Integer> required = integerArgument("-n").required().build();
		CommandLineParser parser = CommandLineParser.forArguments(required);

		try
		{
			parser.parse("-n", "1");
		}
		catch(ArgumentException e)
		{
			fail("Parser failed to handle required argument");
		}
		parser.parse(); // The second time shouldn't be affected by the first
	}

	@SuppressWarnings("deprecation")
	// This is what's tested
	@Test(expected = IllegalStateException.class)
	public void testMakingAnOptionalArgumentRequired()
	{
		optionArgument("-l").required();
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testSettingADefaultValueForARequiredArgument()
	{
		integerArgument("-l").required().defaultValue(42);
	}

	@Test(expected = IllegalStateException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testMakingARequiredArgumentWithDefaultValue()
	{
		integerArgument("-l").defaultValue(42).required();
	}
}
