package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.charArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.StringParsers;

/**
 * Tests for {@link ArgumentFactory#charArgument(String...)} and {@link StringParsers#charParser()}
 */
public class CharArgumentTest
{
	@Test
	public void testValidCharacter() throws ArgumentException
	{
		Character z = charArgument("-c").parse("-c", "Z");

		assertThat(z).isEqualTo('Z');
	}

	@Test
	public void testInvalidLength()
	{
		try
		{
			charArgument("-c").parse("-c", "abc");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("'abc' is not a valid character");
		}
	}

	@Test
	public void testDescription()
	{
		String usage = charArgument("-c").usage();
		assertThat(usage).contains("<character>: any unicode character");
		assertThat(usage).contains("Default: the Null character");

		assertThat(charArgument("-c").defaultValue(null).usage()).contains("Default: null");
		assertThat(charArgument("-c").defaultValue('A').usage()).contains("Default: A");
	}

	@Test
	public void testThatCharDefaultsToZero() throws ArgumentException
	{
		Character c = charArgument("-c").parse();
		assertThat(c.charValue()).isEqualTo((char) 0);
	}
}
