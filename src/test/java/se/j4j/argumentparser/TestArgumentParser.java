package se.j4j.argumentparser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.util.Arrays;
import java.util.List;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.DefaultArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.utils.ArgumentExpector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestArgumentParser
{
	/**
	 * An example of how to create a pretty readable command line invocation:
	 * java testprog --enable-logging --listen-port 8090 Hello
	 */
	@Test
	public void testMixedArgumentTypes() throws ArgumentException
	{
		Argument<Boolean> enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument("-p", "--listen-port").defaultValue(8080).description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();

		ArgumentExpector expector = new ArgumentExpector();

		expector.expectThat(enableLogging).receives(true).given("--enable-logging");

		expector.expectThat(port).receives(8090).given("--listen-port 8090");

		expector.expectThat(greetingPhrase).receives("Hello").given("Hello");
	}

	/**
	 * An example of how to create a pretty unreadable command line invocation:
	 * java testprog true 8090 Hello
	 * Note that the order of the arguments matter.
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

		Argument<Boolean> loggingEnabled = optionArgument("--disable-logging").defaultValue(true)
				.description("Don't output debug information to standard out").build();

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
	public void testMultipleParametersForNamedArgument() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").consumeAll().build();
		List<Integer> actual = ArgumentParser.forArguments(numbers).parse(args).get(numbers);

		assertThat(actual).isEqualTo(Arrays.asList(5, 6));
	}

	@Test
	public void testMultipleParametersForNamedArgumentUnmodifiableResult() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").consumeAll().build();
		List<Integer> actual = ArgumentParser.forArguments(numbers).parse(args).get(numbers);

		try
		{
			actual.add(3);
			Fail.fail("a list of values should be unmodifiable");
		}
		catch(UnsupportedOperationException expected)
		{

		}
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
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testErrorHandlingForTwoParametersWithTheSameName()
	{
		Argument<Integer> number = integerArgument("--number").build();
		ArgumentParser.forArguments(number, number);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testErrorHandlingForTwoIndexedParametersWithTheSameDefinition()
	{
		Argument<Integer> numberOne = integerArgument().build();
		ArgumentParser.forArguments(numberOne, numberOne);
	}

	@Test
	public void testNullAsDefaultValue() throws ArgumentException
	{
		Argument<Integer> number = integerArgument("-n").defaultValue(null).build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse();
		assertThat(parsed.get(number)).isNull();
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = {"NP_NONNULL_PARAM_VIOLATION", "RV_RETURN_VALUE_IGNORED"}, justification = "Expecting fail-fast during construction")
	public void testNullHandler()
	{
		new DefaultArgumentBuilder<Integer>(null).build();
	}

	@Test
	public void testToString() throws ArgumentException
	{
		assertThat(integerArgument().toString()).contains("<integer>: -2147483648 to 2147483647");

		ArgumentParser parser = ArgumentParser.forArguments();
		assertThat(parser.toString()).contains("<main class>");

		assertThat(parser.parse().toString()).isEqualTo("{}");
	}

	@Test
	public void testShorthandInvocation() throws ArgumentException
	{
		assertThat(integerArgument().build().parse("42")).isEqualTo(42);
	}

	@Test(expected = InvalidArgument.class)
	public void testWrongArgumentForShorthandInvocation() throws ArgumentException
	{
		integerArgument().build().parse("a42");
	}

	/**
	 * TODO: add support for @/path/to/arguments
	 */
}
