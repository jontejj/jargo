package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;

public class TestBooleanArguments
{
	@Test
	public void testDescription()
	{
		String usage = booleanArgument("-b").usage("BooleanArgument");
		assertThat(usage).contains("<boolean>: true or false");
	}

	@Test
	public void testThatInvalidValuesIsTreatedAsFalse() throws ArgumentException
	{
		boolean result = booleanArgument("-b").parse("-b", "true or wait, no false");
		assertThat(result).isFalse();
	}

	@Test
	public void testThatBooleanDefaultsToFalse() throws ArgumentException
	{
		boolean result = booleanArgument("-b").parse();
		assertThat(result).isFalse();
	}
}
