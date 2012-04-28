package se.j4j.argumentparser.basichandlers;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.internal.Usage;

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
		Argument<Action> enumArgument = ArgumentFactory.enumArgument(Action.class).build();
		ParsedArguments parsed = ArgumentParser.forArguments(enumArgument).parse("stop");

		assertThat(parsed.get(enumArgument)).isEqualTo(Action.stop);
	}

	@Test
	public void testEnumArgumentDescription()
	{
		Argument<Action> enumArgument = ArgumentFactory.enumArgument(Action.class).build();
		String usageText = Usage.forSingleArgument(enumArgument);
		assertThat(usageText).contains("<value>: [start, stop, restart]");
		assertThat(usageText).contains("Default: null");
	}

	@Test(expected = InvalidArgument.class)
	public void testInvalidEnumArgument() throws ArgumentException
	{
		Argument<Action> enumArgument = ArgumentFactory.enumArgument(Action.class).build();
		ArgumentParser.forArguments(enumArgument).parse("break");
	}
}
