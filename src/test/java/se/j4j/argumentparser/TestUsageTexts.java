package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
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
import static se.j4j.argumentparser.CustomHandlers.DateTimeHandler.dateArgument;

import java.io.File;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Test;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.internal.Usage;
import se.j4j.argumentparser.utils.Lines;

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
			ArgumentParser.forArguments(greetingPhrase, enableLogging, port, longArgument, bigIntegerArgument, date, doubleArgument, shortArgument,
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

		Argument<Map<String, Integer>> defaultingMap = integerArgument("-N").asPropertyMap().defaultValue(defaults).build();

		String usage = Usage.forSingleArgument(defaultingMap);
		assertThat(usage).contains("Default: Hello -> 1, World -> 42");

	}

	@Test
	public void testUsageWithRequiredArguments()
	{
		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

		Argument<String> greetingPhrase = stringArgument("-s").required().description("A greeting phrase to greet new connections with").build();

		String usage = ArgumentParser.forArguments(greetingPhrase, enableLogging).usage("RequiredArgumentDescription").toString();

		assertThat(usage).contains("-l, --enable-logging    Output debug information to standard out");
		assertThat(usage).contains("-s <string>             A greeting phrase to greet new connections with [Required]");
	}


	@Test
	public void testUsageWithRepeatedArguments()
	{
		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

		Argument<List<String>> greetingPhrases = stringArgument("-s").required().repeated()
				.description("A greeting phrase to greet new connections with").build();

		String usage = ArgumentParser.forArguments(greetingPhrases, enableLogging).usage("RepeatedArgumentDescription").toString();

		assertThat(usage).contains("-l, --enable-logging    Output debug information to standard out");
		assertThat(usage).contains("-s <string>             A greeting phrase to greet new connections with [Required] [Supports Multiple occurences]");
	}

	@Test
	public void testUsageForNoArguments()
	{
		// TODO: add possibility to add a description of the program as a whole
		String usage = ArgumentParser.forArguments().usage("NoArguments").toString();
		assertThat(usage).isEqualTo("Usage: NoArguments");
	}

	@Test
	public void testUsageWithArguments()
	{
		String usage = ArgumentParser.forArguments(stringArgument().build()).usage("SomeArguments").toString();
		assertThat(usage).startsWith("Usage: SomeArguments [Options]");
	}

	@Test
	public void testHiddenArguments() throws ArgumentException
	{
		Argument<String> hiddenArgument = stringArgument().hideFromUsage().build();
		ArgumentParser parser = ArgumentParser.forArguments(hiddenArgument);
		String usage = parser.usage("HiddenArgument").toString();

		assertThat(usage).isEqualTo("Usage: HiddenArgument [Options]" + Lines.NEWLINE);

		assertThat(parser.parse("hello").get(hiddenArgument)).isEqualTo("hello");
	}
	
	@Test
	public void testUsageTextForDefaultEmptyList()
	{
		String usage = ArgumentParser.forArguments(stringArgument().arity(2).build()).usage("DefaultEmptyList").toString();
		assertThat(usage).contains("Default: Empty list");
	}
	
	@Test
	public void testUsageTextForDefaultList()
	{
		Argument<List<Integer>> arg = integerArgument().defaultValue(1).repeated().build();
		String usage = ArgumentParser.forArguments(arg).usage("DefaultList").toString();
		assertThat(usage).contains("Default: [1]");
	}
}
