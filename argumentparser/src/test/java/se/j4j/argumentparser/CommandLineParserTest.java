package se.j4j.argumentparser;

import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.CommandLineParser.withArguments;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.fest.assertions.Fail;
import org.junit.Ignore;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.argumentparser.utils.ArgumentExpector;
import se.j4j.testlib.Explanation;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

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

		assertThat(listResult).isEqualTo(arrayResult);
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

	@Test
	public void testThatInputIsCopiedBeforeBeingWorkedOn() throws ArgumentException
	{
		List<String> args = Arrays.asList("-Dfoo=bar", "-Dbaz=zoo");
		List<String> copy = Lists.newArrayList(args);
		Argument<Map<String, String>> arg = stringArgument("-D").asPropertyMap().build();
		CommandLineParser.withArguments(arg).parse(args);
		assertThat(args).isEqualTo(copy);
	}

	@Test
	public void testThatQuotesAreNotTrimmedAsTheShellIsResponsibleForThat() throws ArgumentException
	{
		assertThat(stringArgument().parse("\"hello\"")).isEqualTo("\"hello\"");
	}

	/**
	 * TODO: Supporting @/path/to/arguments style arguments has the obvious
	 * limitation that arguments starting with @ and that don't want the
	 * argument to be parsed as a filename can't be accepted,
	 * can quotes be used to circumvent this?
	 */
	@Ignore
	@Test
	public void testReadingArgumentsFromFile() throws IOException, ArgumentException
	{
		File tempFile = File.createTempFile("_testReadingArgumentsFromFile", ".arguments");
		tempFile.deleteOnExit();

		Files.write(Joiner.on(NEWLINE).join("hello", "world"), tempFile, Charsets.UTF_8);

		List<String> parsed = stringArgument().variableArity().parse("@" + tempFile.getPath());

		assertThat(parsed).isEqualTo(Arrays.asList("hello", "world"));
	}
}
