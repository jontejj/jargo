package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestDefaultHandlers
{
	public enum Action
	{
		start, stop, restart
	}

	@Test
	public void testEnumArgument() throws ArgumentException
	{
		Argument<Action> enumArgument = ArgumentFactory.enumArgument(Action.class).build();
		ParsedArguments parsed = ArgumentParser.forArguments(enumArgument).parse("stop");

		assertThat(parsed.get(enumArgument)).isEqualTo(Action.stop);
	}

}
