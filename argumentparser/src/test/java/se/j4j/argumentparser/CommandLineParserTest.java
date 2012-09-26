package se.j4j.argumentparser;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.CommandLineParser.withArguments;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import java.util.Arrays;

import org.fest.assertions.Fail;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.argumentparser.utils.ArgumentExpector;
import se.j4j.testlib.Explanation;
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

	@Test
	public void testShorthandInvocation() throws ArgumentException
	{
		// For a single argument it's easier to call parse directly on the ArgumentBuilder
		assertThat(integerArgument("-n").parse("-n", "42")).isEqualTo(42);
	}

	@Test(expected = UnexpectedArgumentException.class)
	public void testUnhandledParameter() throws ArgumentException
	{
		CommandLineParser.withArguments().parse("Unhandled");
	}

	@Test
	public void testMissingParameterForArgument()
	{
		Argument<Integer> numbers = integerArgument("--number", "-n").build();
		try
		{
			numbers.parse("-n");
			fail("-n parameter should be missing a parameter");
		}
		catch(ArgumentException expected)
		{
			// As -n was given on the command line that is the one that should appear in the
			// exception message
			assertThat(expected.getMessageAndUsage("MissingParameterTest")).isEqualTo(expected("missingParameter"));
		}
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

	@Test(expected = ArgumentException.class)
	public void testWrongArgumentForShorthandInvocation() throws ArgumentException
	{
		integerArgument().parse("a42");
	}

	@Test
	public void testThatCloseMatchIsSuggestedForTypos()
	{
		try
		{
			integerArgument("-n", "--number").parse("-number");
			fail("-number should have to be --number");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format(UserErrors.SUGGESTION, "-number", "--number"));
		}
	}

	@Test
	public void testListInterface() throws ArgumentException
	{
		String[] args = {"--number", "1"};

		Argument<Integer> number = integerArgument("--number").build();

		ParsedArguments listResult = CommandLineParser.withArguments(Arrays.<Argument<?>>asList(number)).parse(Arrays.asList(args));
		ParsedArguments arrayResult = CommandLineParser.withArguments(number).parse(args);
		ParsedArguments iteratorResult = CommandLineParser.withArguments(Arrays.<Argument<?>>asList(number))
				.parse(Arrays.asList(args).listIterator());

		assertThat(listResult).isEqualTo(arrayResult);
		assertThat(iteratorResult).isEqualTo(arrayResult);
	}

	@Test
	public void testEqualsAndHashcodeForParsedArguments() throws ArgumentException
	{
		Argument<Integer> number = integerArgument("--number").build();

		CommandLineParser parser = CommandLineParser.withArguments(number);
		ParsedArguments parsedArguments = parser.parse();

		assertThat(parsedArguments).isNotEqualTo(null);
		assertThat(parsedArguments).isEqualTo(parsedArguments);

		ParsedArguments parsedArgumentsTwo = parser.parse();

		assertThat(parsedArguments.hashCode()).isEqualTo(parsedArgumentsTwo.hashCode());

		assertThat(withArguments().parse()).isEqualTo(withArguments().parse());
	}

	@Test
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatNameCollisionAmongTwoDifferentArgumentsIsDetected()
	{
		Argument<Integer> numberOne = integerArgument("-n", "-s").build();
		Argument<Integer> numberTwo = integerArgument("-t", "-s").build();
		try
		{
			CommandLineParser.withArguments(numberOne, numberTwo);
			Fail.fail("Duplicate -s name not detected");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage(String.format(ProgrammaticErrors.NAME_COLLISION, "-s"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testErrorHandlingForTwoIndexedParametersWithTheSameDefinition()
	{
		Argument<Integer> numberOne = integerArgument().build();
		// There wouldn't be a way to know which argument numberOne referenced
		CommandLineParser.withArguments(numberOne, numberOne);
	}

	@Test
	public void testThatArgumentNotGivenIsIllegalArgument() throws ArgumentException
	{
		Argument<Integer> numberOne = integerArgument().build();
		Argument<Integer> numberTwo = integerArgument().build();
		try
		{
			CommandLineParser.withArguments(numberOne).parse().get(numberTwo);
			fail("numberTwo should be illegal as only numberOne is handled");
		}
		catch(IllegalArgumentException e)
		{
			assertThat(e).hasMessage(String.format(ProgrammaticErrors.ILLEGAL_ARGUMENT, numberTwo));
		}
	}

	@Test
	public void testThatItsIllegalToPassNullInArguments() throws ArgumentException
	{
		try
		{
			integerArgument().parse(null, null);
		}
		catch(NullPointerException expected)
		{
			assertThat(expected).hasMessage("Argument strings may not be null");
		}
	}

	/**
	 * TODO: add support for @/path/to/arguments
	 */
}
