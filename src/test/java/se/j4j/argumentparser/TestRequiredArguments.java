package se.j4j.argumentparser;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestRequiredArguments
{

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

	@Test(expected = MissingRequiredArgumentException.class)
	public void testThatRequiredArgumentsIsResetBetweenParsings() throws ArgumentException
	{
		Argument<Integer> required = integerArgument("-n").required().build();
		ArgumentParser parser = ArgumentParser.forArguments(required);

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

	@Test
	public void testMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {};

		Argument<Integer> number = integerArgument("--number").required().build();
		Argument<Integer> number2 = integerArgument("--number2").required().build();

		try
		{
			ParsedArguments parsed = ArgumentParser.forArguments(number, number2).parse(args);
			fail("Required argument silently ignored. Parsed data: " + parsed);
		}
		catch(MissingRequiredArgumentException ex)
		{
			assertThat(ex.missingArguments()).contains(number, number2);
			System.out.println(ex.getMessage());
			// TODO: assert error text
		}
	}
}
