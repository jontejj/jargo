package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.floatArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;

public class FloatArgumentTest
{
	@Test
	public void testValidFloat() throws ArgumentException
	{
		float f = floatArgument("-d").parse("-d", "555.666");

		assertThat(f).isEqualTo(555.666f);
	}

	@Test
	public void testInvalidFloat()
	{
		try
		{
			floatArgument("-f").parse("-f", "1,a");
		}
		catch(ArgumentException e)
		{
			assertThat(e.getMessage()).isEqualTo("'1,a' is not a valid float (32-bit IEEE 754 floating point)");
		}
	}

	@Test
	public void testDescription()
	{
		String usage = floatArgument("-f").usage("FloatArgument");
		assertThat(usage).contains("<float>: -3.4028235E38 to 3.4028235E38");
	}

	@Test
	public void testThatFloatDefaultsToZero() throws ArgumentException
	{
		float f = floatArgument("-f").parse();
		assertThat(f).isEqualTo(0.0f);
	}
}
