package se.j4j.argumentparser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArithmeticArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.exceptions.UnexpectedArgumentException;

public class TestArgumentParser
{
	/**
	 * An example of how to create a pretty readable command line invocation:
	 * java testprog --enable-logging --listen-port 8090 Hello
	 */
	@Test
	public void testMixedArgumentTypes() throws ArgumentException
	{
		String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};

		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").
				description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument("-p", "--listen-port").defaultValue(8080).
				description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();

		ParsedArguments arguments = ArgumentParser.forArguments(greetingPhrase, enableLogging, port).parse(args);

		assertTrue(enableLogging + " was not found in parsed arguments", arguments.get(enableLogging));
		assertEqual(port + " was not found in parsed arguments", 8090, arguments.get(port));
		assertEquals(greetingPhrase + " was not found in parsed arguments", "Hello", arguments.get(greetingPhrase));
	}

	/**
	 * An example of how to create a pretty unreadable command line invocation:
	 * java testprog true 8090 Hello
	 *
	 * Note that the order of the arguments matter.
	 *
	 * Please don't overuse this:)
	 */
	@Test
	public void testIndexedArguments() throws ArgumentException
	{
		String[] args = {"true", "8090", "Hello"};

		Argument<Boolean> enableLogging = booleanArgument().description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument().defaultValue(8080).description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();

		ParsedArguments arguments = ArgumentParser.forArguments(enableLogging, port, greetingPhrase).parse(args);

		assertTrue(enableLogging + " was not found in parsed arguments", arguments.get(enableLogging));
		assertEqual(port + " was not found in parsed arguments", 8090, arguments.get(port));
		assertEquals(greetingPhrase + " was not found in parsed arguments", "Hello", arguments.get(greetingPhrase));
	}

	@Test
	public void testFlagArgumentDefaultValue() throws ArgumentException
	{
		String[] args = {};

		Argument<Boolean> loggingEnabled = optionArgument("--disable-logging").defaultValue(true).
				description("Don't output debug information to standard out").build();

		ParsedArguments arguments = ArgumentParser.forArguments(loggingEnabled).parse(args);

		assertTrue(loggingEnabled + " did not default to true", arguments.get(loggingEnabled));
	}
	@Test(expected = UnexpectedArgumentException.class)
	public void testUnhandledParameter() throws ArgumentException
	{
		String[] args = {"Unhandled"};
		ArgumentParser.forArguments().parse(args);
	}

	@Test
	public void testArithmeticArgumentAddOperation() throws ArgumentException
	{
		String[] args = {"--sum-elements", "5", "6", "-3"};

		Argument<Integer> sum = integerArithmeticArgument("--sum-elements").defaultValue(2).operation('+').build();
		assertEqual("Elements should have been summed together", 8, ArgumentParser.forArguments(sum).parse(args).get(sum));
	}

	@Test
	public void testMultipleParametersForNamedArgument() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").consumeAll().build();

		assertEqual("", Arrays.asList(5, 6), ArgumentParser.forArguments(numbers).parse(args).get(numbers));
	}

	@Test
	public void testMissingParameterForArgument()
	{
		String[] args = {"--number"};

		Argument<Integer> numbers = integerArgument("--number").build();

		try
		{
			ArgumentParser.forArguments(numbers).parse(args);
			fail("--number parameter should be missing a parameter");
		}
		catch(ArgumentException exception)
		{
			assertEquals(ArgumentExceptionCodes.MISSING_PARAMETER, exception.code());
			assertEquals(numbers, exception.errorneousArgument());
		}
	}

	@Test
	public void testDefaultValuesForMultipleParametersForNamedArgument() throws ArgumentException
	{
		String[] args = {};

		List<Integer> defaults = Arrays.asList(5, 6);

		Argument<List<Integer>> numbers = integerArgument("--numbers").consumeAll().defaultValue(defaults).build();

		assertEqual("", defaults, ArgumentParser.forArguments(numbers).parse(args).get(numbers));
	}

	@Test
	public void testTwoParametersForNamedArgument() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "Rest", "Of", "Arguments"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").arity(2).build();
		Argument<List<String>> restHandler = stringArgument().consumeAll().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers, restHandler).parse(args);

		assertEqual("", Arrays.asList(5, 6), parsed.get(numbers));
		assertEqual("", Arrays.asList("Rest", "Of", "Arguments"), parsed.get(restHandler));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorHandlingForTwoParametersWithTheSameName()
	{
		Argument<Integer> number = integerArgument("--number").build();
		Argument<Integer> numberTwo = integerArgument("--number").build();

		ArgumentParser.forArguments(number, numberTwo);
	}

	@Test
	public void testIgnoringCase() throws ArgumentException
	{
		Argument<Boolean> help = optionArgument("-h", "--help", "-help", "?").ignoreCase().build();

		ArgumentParser parser = ArgumentParser.forArguments(help);

		assertTrue("unhandled capital letter for ignore case argument", parser.parse("-H").get(help));
		assertTrue(parser.parse("-HELP").get(help));
		assertTrue(parser.parse("--help").get(help));
	}

	/**
	 * TODO: add support for @/path/to/arguments
	 * TODO:
	 */
	private static <T> void assertEqual(final String message, final T expected, final T actual)
	{
		assertEquals(message, expected, actual);
	}
}
