package se.j4j.argumentparser.basichandlers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.handlers.RadixiableArgument.Radix.BINARY;
import static se.j4j.argumentparser.handlers.RadixiableArgument.Radix.HEX;
import static se.j4j.argumentparser.handlers.RadixiableArgument.Radix.OCTAL;

import org.junit.Test;

import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestRadixArguments
{
	@Test
	public void testOctalInput() throws ArgumentException
	{
		String input = Integer.toString(0112, OCTAL.radix());
		Integer octal = integerArgument("-o").radix(OCTAL).parse("-o", input);
		assertThat(octal).isEqualTo(0112);
	}

	@Test
	public void testHexInput() throws ArgumentException
	{
		Integer hex = integerArgument("-h").radix(HEX).parse("-h", "FF");
		assertThat(hex).isEqualTo(0xFF);
	}

	@Test
	public void testBinaryInput() throws ArgumentException
	{
		Integer binary = integerArgument("-b").radix(BINARY).parse("-b", "1001");
		assertThat(binary).isEqualTo(9);
	}
}
