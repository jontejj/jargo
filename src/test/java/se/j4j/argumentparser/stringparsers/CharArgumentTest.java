package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.charArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;

public class CharArgumentTest
{
	@Test
	public void testValidCharacter() throws ArgumentException
	{
		Character z = charArgument("-c").parse("-c", "Z");

		assertThat(z).isEqualTo('Z');
	}

	@Test
	public void testInvalidLength() throws ArgumentException
	{
		try
		{
			charArgument("-c").parse("-c", "abc");
		}
		catch(InvalidArgument e)
		{
			assertThat(e.getMessage()).isEqualTo("'abc' is not a valid character");
		}
	}

	@Test
	public void testDescription()
	{
		String usage = charArgument("-c").usage("CharArgument");
		assertThat(usage).contains("<character>: any unicode character");
		assertThat(usage).contains("Default: the Null character");

		assertThat(charArgument("-c").defaultValue(null).usage("CharArgument")).contains("Default: null");
		assertThat(charArgument("-c").defaultValue('A').usage("CharArgument")).contains("Default: A");
	}

	@Test
	public void testThatCharDefaultsToZero() throws ArgumentException
	{
		Character c = charArgument("-c").parse();
		assertThat(c.charValue()).isEqualTo((char) 0);
	}
}
