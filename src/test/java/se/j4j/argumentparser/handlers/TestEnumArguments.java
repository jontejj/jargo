package se.j4j.argumentparser.handlers;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

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
		Action enumArgument = ArgumentFactory.enumArgument(Action.class).parse("stop");
		assertThat(enumArgument).isEqualTo(Action.stop);
	}

	@Test
	public void testEnumArgumentDescription()
	{
		Argument<Action> enumArgument = ArgumentFactory.enumArgument(Action.class).build();
		String usageText = enumArgument.toString();
		assertThat(usageText).contains("<value>: [start, stop, restart]");
		assertThat(usageText).contains("Default: null");
	}

	@Test(expected = InvalidArgument.class)
	public void testInvalidEnumArgument() throws ArgumentException
	{
		ArgumentFactory.enumArgument(Action.class).parse("break");
	}
}
