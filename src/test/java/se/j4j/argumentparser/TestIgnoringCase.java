package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class TestIgnoringCase
{

	@Test
	public void testIgnoringCase() throws ArgumentException
	{
		Argument<Boolean> help = optionArgument("-h", "--help", "-help", "?").ignoreCase().build();

		CommandLineParser parser = CommandLineParser.forArguments(help);

		assertThat(parser.parse("-H").get(help)).as("unhandled capital letter for ignore case argument").isTrue();
		assertThat(parser.parse("-HELP").get(help)).isTrue();
		assertThat(parser.parse("--help").get(help)).isTrue();
	}

	@Test
	public void testWithPropertyMap() throws ArgumentException
	{
		Map<String, Integer> numbers = integerArgument("-n").asPropertyMap().ignoreCase().parse("-nsmall=1", "-Nbig=5");

		Map<String, Integer> expected = new HashMap<String, Integer>();
		expected.put("small", 1);
		expected.put("big", 5);
		assertThat(numbers).isEqualTo(expected);
	}
}
