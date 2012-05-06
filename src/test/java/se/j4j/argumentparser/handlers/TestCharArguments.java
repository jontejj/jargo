package se.j4j.argumentparser.handlers;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class TestCharArguments
{

	@Test(expected = InvalidArgument.class)
	public void testInvalidLength() throws ArgumentException
	{
		ArgumentFactory.charArgument("-c").parse("-c", "Character should be of length one");
	}

	@Test
	public void testValidCharacter() throws ArgumentException
	{
		Character z = ArgumentFactory.charArgument("-c").parse("-c", "Z");

		assertThat(z).isEqualTo('Z');
	}
}
