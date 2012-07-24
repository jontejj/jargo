package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;

import java.util.Map;

import org.junit.Test;

public class TestIgnoringCase
{
	@Test
	public void testIgnoringCase() throws ArgumentException
	{
		Argument<Boolean> help = optionArgument("-h", "--help", "-help", "?").ignoreCase().build();

		assertThat(help.parse("-H")).as("unhandled capital letter for ignore case argument").isTrue();
		assertThat(help.parse("-HELP")).isTrue();
		assertThat(help.parse("--help")).isTrue();
	}

	@Test
	public void testWithPropertyMap() throws ArgumentException
	{
		Map<String, Integer> numbers = integerArgument("-n").asPropertyMap().ignoreCase().parse("-nsmall=1", "-Nbig=5");

		assertThat(numbers.get("small")).isEqualTo(1);
		assertThat(numbers.get("big")).isEqualTo(5);
	}
}
