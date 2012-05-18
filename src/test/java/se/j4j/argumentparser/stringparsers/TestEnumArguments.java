package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.enumArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;

public class TestEnumArguments
{
	public enum Action
	{
		start,
		stop,
		restart
	}

	@Test
	public void testEnumArgument() throws ArgumentException
	{
		Action action = enumArgument(Action.class).parse("stop");
		assertThat(action).isEqualTo(Action.stop);
	}

	@Test
	public void testEnumArgumentDescription()
	{
		String usageText = enumArgument(Action.class).usage("");
		assertThat(usageText).contains("<value>: [start, stop, restart]");
		assertThat(usageText).contains("Default: null");
	}

	@Test
	public void testInvalidEnumArgument() throws ArgumentException
	{
		try
		{
			enumArgument(Action.class).parse("break");
		}
		catch(InvalidArgument e)
		{
			assertThat(e.getMessage()).isEqualTo("'break' is not a valid Option, Expecting one of [start, stop, restart]");
		}
	}
}
