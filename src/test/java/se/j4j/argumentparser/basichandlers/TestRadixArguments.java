package se.j4j.argumentparser.basichandlers;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.handlers.RadixiableArgument.Radix.BINARY;
import static se.j4j.argumentparser.handlers.RadixiableArgument.Radix.HEX;
import static se.j4j.argumentparser.handlers.RadixiableArgument.Radix.OCTAL;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestRadixArguments
{
	@Test
	public void testOctalInput() throws ArgumentException
	{
		Argument<Integer> octal = integerArgument("-o").radix(OCTAL).build();

		String input = Integer.toString(0112, OCTAL.radix());
		ParsedArguments parsed = ArgumentParser.forArguments(octal).parse("-o", input);

		assertThat(parsed.get(octal)).isEqualTo(0112);

	}

	@Test
	public void testHexInput() throws ArgumentException
	{
		Argument<Integer> octal = integerArgument("-h").radix(HEX).build();

		ParsedArguments parsed = ArgumentParser.forArguments(octal).parse("-h", "FF");

		assertThat(parsed.get(octal)).isEqualTo(255);
	}

	@Test
	public void testBinaryInput() throws ArgumentException
	{
		Argument<Integer> octal = integerArgument("-b").radix(BINARY).build();

		ParsedArguments parsed = ArgumentParser.forArguments(octal).parse("-b", "1001");

		assertThat(parsed.get(octal)).isEqualTo(9);
	}
}
