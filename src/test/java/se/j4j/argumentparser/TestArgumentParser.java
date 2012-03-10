package se.j4j.argumentparser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
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
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

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

		assertThat(arguments.get(enableLogging)).isTrue();
		assertThat(arguments.get(port)).isEqualTo(8090);
		assertThat(arguments.get(greetingPhrase)).isEqualTo("Hello");
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

		assertThat(arguments.get(enableLogging)).isTrue();
		assertThat(arguments.get(port)).isEqualTo(8090);
		assertThat(arguments.get(greetingPhrase)).isEqualTo("Hello");
	}

	@Test
	public void testFlagArgumentDefaultValue() throws ArgumentException
	{
		String[] args = {};

		Argument<Boolean> loggingEnabled = optionArgument("--disable-logging").defaultValue(true).
				description("Don't output debug information to standard out").build();

		ParsedArguments arguments = ArgumentParser.forArguments(loggingEnabled).parse(args);

		assertThat(arguments.get(loggingEnabled)).as("defaults to true").isTrue();
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

		int total = ArgumentParser.forArguments(sum).parse(args).get(sum);

		assertThat(total).as("Elements summed together").isEqualTo(8);
	}

	@Test
	public void testMultipleParametersForNamedArgument() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").consumeAll().build();
		List<Integer> actual = ArgumentParser.forArguments(numbers).parse(args).get(numbers);

		assertThat(actual).isEqualTo(Arrays.asList(5, 6));
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
		List<Integer> defaults = Arrays.asList(5, 6);

		Argument<List<Integer>> numbers = integerArgument("--numbers").consumeAll().defaultValue(defaults).build();
		List<Integer> actual = ArgumentParser.forArguments(numbers).parse().get(numbers);

		assertThat(actual).isEqualTo(defaults);
	}

	@Test
	public void testTwoParametersForNamedArgument() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "Rest", "Of", "Arguments"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").arity(2).build();
		Argument<List<String>> restHandler = stringArgument().consumeAll().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers, restHandler).parse(args);

		assertThat(parsed.get(numbers)).isEqualTo(Arrays.asList(5, 6));
		assertThat(parsed.get(restHandler)).isEqualTo(Arrays.asList("Rest", "Of", "Arguments"));
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testErrorHandlingForTwoParametersWithTheSameName()
	{
		Argument<Integer> number = integerArgument("--number").build();
		ArgumentParser.forArguments(number, number);
	}

	/**
	 * TODO: add support for @/path/to/arguments
	 */
}
