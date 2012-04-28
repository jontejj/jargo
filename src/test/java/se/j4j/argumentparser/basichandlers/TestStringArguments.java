package se.j4j.argumentparser.basichandlers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestStringArguments
{
	@Test
	public void testThatTheSameStringIsReturned() throws ArgumentException
	{
		Argument<String> stringArgument = stringArgument("-s").build();
		String argumentValue = "Test";
		ParsedArguments arguments = ArgumentParser.forArguments(stringArgument).parse("-s", argumentValue);
		assertThat(arguments.get(stringArgument)).isSameAs(argumentValue);
	}
}
