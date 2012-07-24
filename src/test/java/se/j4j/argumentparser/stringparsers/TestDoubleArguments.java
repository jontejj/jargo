package se.j4j.argumentparser.stringparsers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.doubleArgument;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;

public class TestDoubleArguments
{
	@Test
	public void testValidDouble() throws ArgumentException
	{
		double d = doubleArgument("-d").parse("-d", "555.666");

		assertThat(d).isEqualTo(555.666);
	}

	@Test
	public void testInvalidDouble() throws ArgumentException
	{
		try
		{
			doubleArgument("-d").parse("-d", "1,a");
		}
		catch(InvalidArgument e)
		{
			assertThat(e.getMessage()).isEqualTo("'1,a' is not a valid double (64-bit IEEE 754 floating point)");
		}
	}

	@Test
	public void testDescription()
	{
		String usage = doubleArgument("-d").usage("DoubleArgument");
		assertThat(usage).contains("<double>: -1.7976931348623157E308 to 1.7976931348623157E308");
	}

	@Test
	public void testThatDoubleDefaultsToZero() throws ArgumentException
	{
		double d = doubleArgument("-d").parse();
		assertThat(d).isEqualTo(0.0);
	}
}
