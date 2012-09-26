package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.StringParsers.optionParser;
import static se.j4j.testlib.UtilityClassTester.testUtilityClassDesign;

import org.junit.Test;

import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.CommandLineParser.ParserCache;
import se.j4j.argumentparser.StringParsers.StringStringParser;
import se.j4j.argumentparser.commands.Build;
import se.j4j.argumentparser.internal.Texts;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UsageTexts;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.numbers.NumberType;
import se.j4j.strings.Describers;
import se.j4j.strings.Descriptions;
import se.j4j.strings.StringsUtil;
import se.j4j.testlib.EnumTester;

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
	public void testParserToString()
	{
		assertThat(integerArgument().internalParser().toString()).isEqualTo("-2147483648 to 2147483647");
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
	public void testThatOptionalArgumentDefaultsToTheGivenValue()
	{
		assertThat(optionParser(true).defaultValue()).isTrue();
		assertThat(optionParser(false).defaultValue()).isFalse();
	}

	@Test
	public void testConstructorsForUtilityClasses()
	{
		testUtilityClassDesign(	ArgumentFactory.class, StringsUtil.class, Descriptions.class, StringParsers.class, ArgumentExceptions.class,
								Describers.class, Texts.class, ParserCache.class, Texts.class, UserErrors.class, UsageTexts.class,
								ProgrammaticErrors.class);
	}

	@Test
	public void testValueOfAndToStringForEnums()
	{
		EnumTester.testThatToStringIsCompatibleWithValueOfRegardlessOfVisibility(StringStringParser.class);
	}
}
