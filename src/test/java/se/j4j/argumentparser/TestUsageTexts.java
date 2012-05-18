package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.j4j.argumentparser.ArgumentExceptions.forInvalidValue;
import static se.j4j.argumentparser.ArgumentFactory.bigIntegerArgument;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.byteArgument;
import static se.j4j.argumentparser.ArgumentFactory.charArgument;
import static se.j4j.argumentparser.ArgumentFactory.doubleArgument;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.longArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.shortArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.ArgumentFactory.withParser;
import static se.j4j.argumentparser.StringSplitters.comma;
import static se.j4j.argumentparser.stringparsers.custom.DateTimeParser.dateArgument;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.internal.Lines;
import se.j4j.argumentparser.stringparsers.custom.HostPortParser;
import se.j4j.argumentparser.utils.UsageTexts;

/**
 * @formatter:off
 */
public class TestUsageTexts
{
	public static void main(final String ... strings)
	{
		// TODO: remove main method

		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument("-p", "--listen-port").required().description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().required().description("A greeting phrase to greet new connections with").build();

		Argument<Long> longArgument = longArgument("--long").build();
		Argument<BigInteger> bigIntegerArgument = bigIntegerArgument("--big").build();

		Argument<DateTime> date = dateArgument("--date").build();
		Argument<Double> doubleArgument = doubleArgument("--double").build();

		Argument<Short> shortArgument = shortArgument("--short").build();

		Argument<Byte> byteArgument = byteArgument("--byte").build();

		Argument<File> fileArgument = fileArgument("--file").build();

		Argument<String> stringArgument = stringArgument("--string").build();

		Argument<Character> charArgument = charArgument("--char").build();

		Argument<Boolean> boolArgument = booleanArgument("--bool").build();
		try
		{
			CommandLineParsers.forArguments(greetingPhrase, enableLogging, port, longArgument, bigIntegerArgument, date, doubleArgument, shortArgument,
										byteArgument, fileArgument, stringArgument, charArgument, boolArgument).parse();
		}
		catch(ArgumentException e)
		{
			System.out.println(e.getUsage("TestUsageTexts"));
		}
	}

	@Test
	public void testDefaultValuesInUsageForPropertyMap()
	{
		Map<String, Integer> defaults = new HashMap<String, Integer>();
		defaults.put("World", 42);
		defaults.put("Hello", 1);

		String usage = integerArgument("-N").asPropertyMap().defaultValue(defaults).usage("");
		assertThat(usage).contains("Default: Hello -> 1, World -> 42");

	}

	@Test
	public void testUsageWithRequiredArguments()
	{
		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

		Argument<String> greetingPhrase = stringArgument("-s").required().description("A greeting phrase to greet new connections with").build();

		String usage = CommandLineParsers.forArguments(greetingPhrase, enableLogging).usage("RequiredArgumentDescription");

		assertThat(usage).contains("-l, --enable-logging    Output debug information to standard out");
		assertThat(usage).contains("-s <string>             A greeting phrase to greet new connections with [Required]");
	}


	@Test
	public void testUsageWithRepeatedArguments()
	{
		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

		Argument<List<String>> greetingPhrases = stringArgument("-s").required().repeated()
				.description("A greeting phrase to greet new connections with").build();

		String usage = CommandLineParsers.forArguments(greetingPhrases, enableLogging).usage("RepeatedArgumentDescription");

		assertThat(usage).contains("-l, --enable-logging    Output debug information to standard out");
		assertThat(usage).contains("-s <string>             A greeting phrase to greet new connections with [Required] [Supports Multiple occurences]");
	}

	@Test
	public void testUsageWithUnallowedRepeationOfArgument() throws ArgumentException
	{
		try
		{
			stringArgument("-s").parse("-s", "foo", "-s", "bar");
			fail("Didn't handle repeated argument by throwing");
		}
		catch(UnhandledRepeatedArgument e)
		{
			String expectedText = UsageTexts.get("unhandledRepition.txt");
			assertThat(e.getMessageAndUsage("NonAllowedRepition")).isEqualTo(expectedText);
		}
	}

	@Test
	public void testUsageForNoArguments()
	{
		// TODO: add possibility to add a description of the program as a whole
		String usage = CommandLineParsers.forArguments().usage("NoArguments");
		assertThat(usage).isEqualTo("Usage: NoArguments");
	}

	@Test
	public void testUsageWithArguments()
	{
		String usage = stringArgument().usage("SomeArguments");
		assertThat(usage).startsWith("Usage: SomeArguments [Options]");
	}

	@Test
	public void testHiddenArguments() throws ArgumentException
	{
		Argument<String> hiddenArgument = stringArgument().hideFromUsage().build();
		CommandLineParser parser = CommandLineParsers.forArguments(hiddenArgument);
		String usage = parser.usage("HiddenArgument").toString();

		assertThat(usage).isEqualTo("Usage: HiddenArgument [Options]" + Lines.NEWLINE);

		assertThat(parser.parse("hello").get(hiddenArgument)).isEqualTo("hello");
	}

	@Test
	public void testUsageTextForDefaultEmptyList()
	{
		String usage = stringArgument().arity(2).usage("DefaultEmptyList");
		assertThat(usage).contains("Default: Empty list");
	}

	@Test
	public void testUsageTextForDefaultList()
	{
		String usage = integerArgument().defaultValue(1).repeated().usage("DefaultList");
		assertThat(usage).contains("Default: [1]");
	}

	@Test
	public void testUsageTextForSplitArguments()
	{
		String usage = integerArgument().splitWith(comma()).usage("SplitArgs");
		assertThat(usage).contains("<integer>: -2147483648 to 2147483647 (decimal), separated by a comma");
	}

	@Test
	public void testEmptyMetaDescription()
	{
		String usage = withParser(new HostPortParser()).description("port:host").usage("EmptyMeta");
		//"Valid input" is the place holder for <meta>
		assertThat(usage).contains("Valid input: port:host");
	}

	@Test
	public void testThatUsageOnArgumentExceptionThrowsWhenNoUsageIsAvailable()
	{
		try
		{
			throw forInvalidValue("Invalid", "Explanation");
		}
		catch(ArgumentException e)
		{
			try
			{
				e.getUsage("ProgramName");
				fail("getUsage should throw when not enough information is available to produce a sane usage text");
			}
			catch(IllegalStateException illegalState)
			{
				assertThat(illegalState).hasMessage("No originParser set for ArgumentException. No usage available.");
			}
		}
	}

	@Test
	public void testSortingOrderForIndexedArguments()
	{
		Argument<String> indexOne = stringArgument().description("IndexOne").build();
		Argument<String> indexTwo = stringArgument().description("IndexTwo").build();
		Argument<String> indexThree = stringArgument().description("IndexThree").build();
		Argument<String> namedOne = stringArgument("-S").build();
		Argument<String> namedTwo = stringArgument("-T").build();
		String usage = CommandLineParsers.forArguments(indexOne, indexTwo, namedOne, indexThree, namedTwo).usage("SortingOfIndexedArguments");

		assertThat(usage).isEqualTo(UsageTexts.get("indexedArgumentsSortingOrder.txt"));
	}

	@Test
	public void testUnexpectedArgument() throws ArgumentException
	{
		try
		{
			integerArgument("--number").parse("--number", "1", "foo");
			fail("foo should cause a throw as it's not handled");
		}
		catch(UnexpectedArgumentException e)
		{
			String expectedMessageAndUsage = UsageTexts.get("unexpectedArgument.txt");
			assertThat(e.getMessageAndUsage("DidNotExpectFoo")).isEqualTo(expectedMessageAndUsage);
		}
	}

	@Test
	public void testUnexpectedArgumentWithoutPreviousArgument() throws ArgumentException
	{
		try
		{
			integerArgument("--number").parse("foo");
			fail("foo should cause a throw as it's not handled");
		}
		catch(UnexpectedArgumentException e)
		{
			String expectedMessageAndUsage = UsageTexts.get("unexpectedArgumentWithoutPrevious.txt");
			assertThat(e.getMessageAndUsage("DidNotExpectFoo")).isEqualTo(expectedMessageAndUsage);
		}
	}
}
