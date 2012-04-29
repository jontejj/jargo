package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestSeparators
{
	@Test
	public void testIgnoringCaseCombinedWithSeperator() throws ArgumentException
	{
		Argument<String> logLevel = stringArgument("-log").ignoreCase().separator("=").build();

		ArgumentParser parser = ArgumentParser.forArguments(logLevel);

		assertThat(parser.parse("-Log=debug").get(logLevel)).as("wrong log level").isEqualTo("debug");
		assertThat(parser.parse("-log=debug").get(logLevel)).as("wrong log level").isEqualTo("debug");
	}

	@Test
	public void testIgnoringCaseCombinedWithAlphaSeperator() throws ArgumentException
	{
		Argument<String> logLevel = stringArgument("-log").ignoreCase().separator("A").build();

		ArgumentParser parser = ArgumentParser.forArguments(logLevel);

		assertThat(parser.parse("-LogAdebug").get(logLevel)).as("wrong log level").isEqualTo("debug");
		assertThat(parser.parse("-logAdebug").get(logLevel)).as("wrong log level").isEqualTo("debug");
	}

	@Test
	public void testArityCombinedWithSeperator() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-numbers").arity(3).separator("=").parse("-numbers=1", "2", "3");

		assertThat(numbers).isEqualTo(Arrays.asList(1, 2, 3));
	}
}
