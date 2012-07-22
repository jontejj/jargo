package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestSeparators
{
	@Test
	public void testIgnoringCaseCombinedWithSeparator() throws ArgumentException
	{
		Argument<String> logLevel = stringArgument("-log").ignoreCase().separator("=").build();

		CommandLineParser parser = CommandLineParser.forArguments(logLevel);

		assertThat(parser.parse("-Log=debug").get(logLevel)).as("wrong log level").isEqualTo("debug");
		assertThat(parser.parse("-log=debug").get(logLevel)).as("wrong log level").isEqualTo("debug");
	}

	@Test
	public void testIgnoringCaseCombinedWithAlphaSeparator() throws ArgumentException
	{
		Argument<String> logLevel = stringArgument("-log").ignoreCase().separator("A").build();

		CommandLineParser parser = CommandLineParser.forArguments(logLevel);

		assertThat(parser.parse("-LogAdebug").get(logLevel)).as("wrong log level").isEqualTo("debug");
		assertThat(parser.parse("-logAdebug").get(logLevel)).as("wrong log level").isEqualTo("debug");
	}

	@Test
	public void testArityCombinedWithSeparator() throws ArgumentException
	{
		List<Integer> numbers = integerArgument("-numbers").arity(3).separator("=").parse("-numbers=1", "2", "3");
		assertThat(numbers).isEqualTo(Arrays.asList(1, 2, 3));
	}

	@Test
	public void testEmptySeparator() throws ArgumentException
	{
		Integer number = integerArgument("-N").separator("").parse("-N10");

		assertThat(number).isEqualTo(10);
	}

	@Test
	public void testEmptySeparatorWithSeveralNames() throws ArgumentException
	{
		Integer number = integerArgument("-N", "--name").separator("").parse("--name10");

		assertThat(number).isEqualTo(10);
	}

	@Test
	public void testEmptySeparatorWithSeveralNamesAndIgnoreCase() throws ArgumentException
	{
		Integer number = integerArgument("-N", "--name").separator("").ignoreCase().parse("--Name10");

		assertThat(number).isEqualTo(10);
	}

	@Test
	public void testTwoLetterSeperator() throws ArgumentException
	{
		Integer number = integerArgument("-N").separator("==").parse("-N==10");

		assertThat(number).isEqualTo(10);
	}

	@Test
	public void testTwoLetterSeperatorWithIgnoreCase() throws ArgumentException
	{
		Integer number = integerArgument("-N").separator("Fo").ignoreCase().parse("-Nfo10");

		assertThat(number).isEqualTo(10);
	}

	@Test
	public void testThatSeparatorIsPrintedBetweenArgumentNameAndMetaDescription()
	{
		String usage = integerArgument("-N").separator("=").usage("SeparatorBetweenNameAndMeta");

		assertThat(usage).isEqualTo(expected("separatorBetweenNameAndMeta"));
	}
}
