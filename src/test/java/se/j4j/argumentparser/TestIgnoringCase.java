package se.j4j.argumentparser;

import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class TestIgnoringCase
{

	@Test
	public void testIgnoringCase() throws ArgumentException
	{
		Argument<Boolean> help = optionArgument("-h", "--help", "-help", "?").ignoreCase().build();

		ArgumentParser parser = ArgumentParser.forArguments(help);

		assertTrue("unhandled capital letter for ignore case argument", parser.parse("-H").get(help));
		assertTrue(parser.parse("-HELP").get(help));
		assertTrue(parser.parse("--help").get(help));
	}


	@Test
	public void testWithPropertyMap() throws ArgumentException
	{
		Argument<Map<String, Integer>> numbers = integerArgument("-n")
				.asPropertyMap()
				.ignoreCase()
				.build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse("-nsmall=1", "-Nbig=5");

		Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put("small", 1);
		expected.put("big", 5);
		assertThat(parsed.get(numbers)).isEqualTo(expected);
	}
}
