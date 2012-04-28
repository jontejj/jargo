package se.j4j.argumentparser.basichandlers;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class TestCharArguments
{

	@Test(expected = InvalidArgument.class)
	public void testInvalidLength() throws ArgumentException
	{
		Argument<Character> charArgument = ArgumentFactory.charArgument("-c").build();

		ArgumentParser.forArguments(charArgument).parse("-c", "Character should be of length one");
	}

	@Test
	public void testValidCharacter() throws ArgumentException
	{
		Argument<Character> charArgument = ArgumentFactory.charArgument("-c").build();

		ParsedArguments arguments = ArgumentParser.forArguments(charArgument).parse("-c", "Z");
		assertThat(arguments.get(charArgument)).isEqualTo('Z');
	}
}
