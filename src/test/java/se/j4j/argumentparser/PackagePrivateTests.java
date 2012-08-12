package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.StringParsers.optionParser;

import org.junit.Test;

import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.commands.Build;
import se.j4j.argumentparser.internal.NumberType;
import se.j4j.argumentparser.internal.PrivateConstructorTest;

/**
 * Tests implementation details that has no meaning in the public API but can serve other purposes
 * such as to ease debugging. These tests can't reside in the internal package for (obvious)
 * visibility problems. They are mostly for code coverage.
 */
public class PackagePrivateTests
{
	@Test
	public void testArgumentToString()
	{
		assertThat(integerArgument().toString()).isEqualTo("<integer>");
		assertThat(integerArgument("-n").toString()).isEqualTo("-n");
	}

	@Test
	public void testCommandLineParserToString() throws ArgumentException
	{
		CommandLineParser parser = CommandLineParser.withArguments();
		assertThat(parser.toString()).contains("CommandLineParser#toString");
		assertThat(parser.parse().toString()).isEqualTo("{}");
	}

	@Test
	public void testArgumentIteratorToString()
	{
		assertThat(ArgumentIterator.forSingleArgument("foobar").toString()).isEqualTo("[foobar]");
	}

	@Test
	public void testNumberTypeToString()
	{
		assertThat(NumberType.INTEGER.toString()).isEqualTo("integer");
	}

	@Test
	public void testCommandToString()
	{
		Build command = new Build();
		assertThat(command.toString()).isEqualTo(command.commandName());
	}

	@Test
	public void testLimiterOkResponseToString()
	{
		assertThat(Limit.OK.toString()).isEqualTo("OK");
	}

	@Test
	public void testThatOptionalArgumentDefaultsToTheGivenValue()
	{
		assertThat(optionParser(true).defaultValue()).isTrue();
		assertThat(optionParser(false).defaultValue()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void testThatNoLimitsCanNotPrintValidValues()
	{
		// Argument should fall back to StringParser#descriptionOfValidValues() instead when no
		// limit is applied
		Limiters.noLimits().descriptionOfValidValues();
	}

	@Test
	public void testPackagePrivateConstructors() throws Exception
	{
		PrivateConstructorTest.callPrivateConstructors(CommandLineParser.ParserCache.class);
	}
}
