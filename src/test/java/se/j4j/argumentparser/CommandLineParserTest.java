package se.j4j.argumentparser;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.CommandLineParser.forArguments;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.util.Arrays;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.InvalidArgument;
import se.j4j.argumentparser.ArgumentExceptions.MissingParameterException;
import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.utils.ArgumentExpector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class CommandLineParserTest
{
	/**
	 * An example of how to create a <b>easy to understand</b> command line invocation:<br>
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
	 * <pre>
	 * An example of how to create a <b>hard to understand</b> command line invocation:
	 * java program true 8090 Hello
	 * Note that the order of the arguments matter and who knows what true
	 * means?
	 * 
	 * This feature is allowed only because there exists some use cases where indexed arguments makes sense, one example is:<br>
	 * echo "Hello World"
	 * </pre>
	 */
	@Test
	public void testIndexedArguments() throws ArgumentException
	{
		String[] args = {"true", "8090", "Hello"};

		Argument<Boolean> enableLogging = booleanArgument().description("Output debug information to standard out").build();

		Argument<Integer> port = integerArgument().defaultValue(8080).description("The port to start the server on.").build();

		Argument<String> greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();

		ParsedArguments arguments = CommandLineParser.forArguments(enableLogging, port, greetingPhrase).parse(args);

		assertThat(arguments.get(enableLogging)).isTrue();
		assertThat(arguments.get(port)).isEqualTo(8090);
		assertThat(arguments.get(greetingPhrase)).isEqualTo("Hello");
	}

	@Test(expected = UnexpectedArgumentException.class)
	public void testUnhandledParameter() throws ArgumentException
	{
		CommandLineParser.forArguments().parse("Unhandled");
	}

	@Test
	public void testMissingParameterForArgument() throws ArgumentException
	{
		Argument<Integer> numbers = integerArgument("--number", "-n").build();
		try
		{
			numbers.parse("-n");
			fail("-n parameter should be missing a parameter");
		}
		catch(MissingParameterException expected)
		{
			// As -n was given on the command line that is the one that should appear in the
			// exception message
			assertThat(expected.getMessageAndUsage("MissingParameterTest")).isEqualTo(expected("missingParameter"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testErrorHandlingForTwoParametersWithTheSameName()
	{
		Argument<Integer> number = integerArgument("--number").build();
		CommandLineParser.forArguments(number, number);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testErrorHandlingForTwoIndexedParametersWithTheSameDefinition()
	{
		Argument<Integer> numberOne = integerArgument().build();
		// There wouldn't be a way to know which argument numberOne referenced
		CommandLineParser.forArguments(numberOne, numberOne);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "NP_NONNULL_PARAM_VIOLATION", justification = "Checks enforcement of the annotation")
	public void testNullMetaDescription()
	{
		// TODO: should this use NullPointerTester in guava-testlib instead?
		integerArgument("-n").metaDescription(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyMetaDescription()
	{
		integerArgument("-n").metaDescription("");
	}

	@Test
	public void testToString() throws ArgumentException
	{
		assertThat(integerArgument().toString()).contains("<integer>: -2147483648 to 2147483647");

		CommandLineParser parser = CommandLineParser.forArguments();
		assertThat(parser.toString()).contains("CommandLineParser#toString");

		assertThat(parser.parse().toString()).isEqualTo("{}");
	}

	@Test
	public void testShorthandInvocation() throws ArgumentException
	{
		// For a single argument it's easier to call parse directly on the ArgumentBuilder
		assertThat(integerArgument().parse("42")).isEqualTo(42);
	}

	@Test(expected = InvalidArgument.class)
	public void testWrongArgumentForShorthandInvocation() throws ArgumentException
	{
		integerArgument().parse("a42");
	}

	@Test
	public void testListInterface() throws ArgumentException
	{
		String[] args = {"--number", "1"};

		Argument<Integer> number = integerArgument("--number").build();

		ParsedArguments listResult = CommandLineParser.forArguments(Arrays.<Argument<?>>asList(number)).parse(Arrays.asList(args));
		ParsedArguments arrayResult = CommandLineParser.forArguments(number).parse(args);
		ParsedArguments iteratorResult = CommandLineParser.forArguments(Arrays.<Argument<?>>asList(number)).parse(Arrays.asList(args).listIterator());

		assertThat(listResult).isEqualTo(arrayResult);
		assertThat(iteratorResult).isEqualTo(arrayResult);
	}

	@Test
	public void testEqualsAndHashcodeForParsedArguments() throws ArgumentException
	{
		Argument<Integer> number = integerArgument("--number").build();

		CommandLineParser parser = CommandLineParser.forArguments(number);
		ParsedArguments parsedArguments = parser.parse();

		assertThat(parsedArguments).isNotEqualTo(null);
		assertThat(parsedArguments).isEqualTo(parsedArguments);

		ParsedArguments parsedArgumentsTwo = parser.parse();

		assertThat(parsedArguments.hashCode()).isEqualTo(parsedArgumentsTwo.hashCode());

		assertThat(forArguments().parse()).isEqualTo(forArguments().parse());
	}

	/**
	 * TODO: add support for @/path/to/arguments
	 */

	// TODO: test serialization of exceptions etc.
}
