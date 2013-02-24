/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
*/
package se.j4j.argumentparser;

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.failure;
import static org.junit.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;
import static se.j4j.argumentparser.CommandLineParser.withArguments;
import static se.j4j.argumentparser.Constants.EXPECTED_TEST_TIME_FOR_THIS_SUITE;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fest.assertions.StringAssert;
import org.junit.Ignore;
import org.junit.Test;

import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.commands.Build;
import se.j4j.argumentparser.commands.Build.BuildTarget;
import se.j4j.argumentparser.commands.Clean;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UserErrors;
import se.j4j.argumentparser.utils.ArgumentExpector;
import se.j4j.testlib.Explanation;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
			assertThat(expected.getMessageAndUsage()).isEqualTo(expected("missingParameter"));
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
	public void testThatAlreadyParsedArgumentsAreNotSuggested()
	{
		try
		{
			integerArgument("-n", "--number").parse("--number", "1", "-number");
			fail("-number should have to be --number");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("Unexpected argument: -number, previous argument: 1");
		}
	}

	@Test
	public void testIterableInterface() throws ArgumentException
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
			fail("Duplicate -s name not detected");
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

	@Test
	public void testThatInvalidArgumentsAddedLaterOnDoesNotWreckTheExistingParser() throws Exception
	{
		Argument<Integer> number = integerArgument("-n").build();
		Argument<Integer> numberTwo = integerArgument("-N").build();
		CommandLineParser parser = CommandLineParser.withArguments(number);

		try
		{
			// Oops meant to add number2
			parser.and(number);
			fail("number should be handled already by parser, adding it again should fail");
		}
		catch(IllegalArgumentException expected)
		{
			// Verify that adding number didn't leave the parser in a weird state by adding the
			// correct argument and parsing it
			parser.and(numberTwo);
			assertThat(parser.parse("-N", "2").get(numberTwo)).isEqualTo(2);
		}
	}

	@Test
	public void testThatInvalidCommandsAddedLaterOnDoesNotWreckTheExistingParser() throws Exception
	{
		BuildTarget target = new BuildTarget();
		CommandLineParser parser = CommandLineParser.withCommands(new Clean(target));

		try
		{
			// Oops meant to add new Build
			parser.and(new Clean(target));
			fail("clean should be handled already by parser, adding it again should fail");
		}
		catch(IllegalArgumentException expected)
		{
			// Verify that adding Clean didn't leave the parser in a weird state by adding
			// the correct argument and parsing it
			parser.and(new Build(target));
			parser.parse("clean", "build");
		}
	}

	@Test
	public void testThatParserIsModifiableAfterFailedProgramDescriptionModification() throws Exception
	{
		testThatNullDoesNotCauseOtherConcurrentUpdatesToFail(new ParserInvocation<String>(){
			@Override
			public void invoke(CommandLineParser parser, String value)
			{
				parser.programDescription(value);
			}
		}, "42").contains("42");
	}

	@Test
	public void testThatParserIsModifiableAfterFailedProgramNameModification() throws Exception
	{
		testThatNullDoesNotCauseOtherConcurrentUpdatesToFail(new ParserInvocation<String>(){
			@Override
			public void invoke(CommandLineParser parser, String value)
			{
				parser.programName(value);
			}
		}, "42").contains("42");
	}

	@Test
	public void testThatParserIsModifiableAfterFailedAddArgumentOperation() throws Exception
	{
		testThatNullDoesNotCauseOtherConcurrentUpdatesToFail(new ParserInvocation<Argument<Integer>>(){
			@Override
			public void invoke(CommandLineParser parser, Argument<Integer> value)
			{
				parser.and(value);
			}
		}, integerArgument("-n").build()).contains("-n");
	}

	private <T> StringAssert testThatNullDoesNotCauseOtherConcurrentUpdatesToFail(final ParserInvocation<T> toInvoke, final @Nonnull T nonnullValue)
			throws InterruptedException
	{
		final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
		final CommandLineParser parser = CommandLineParser.withArguments();

		Thread otherThread = new Thread(){
			@Override
			public void run()
			{
				toInvoke.invoke(parser, nonnullValue);
			};
		};
		otherThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler(){

			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				failure.set(e);
			}
		});

		try
		{
			// Oops meant to use nonnullValue
			toInvoke.invoke(parser, null);
			throw failure("null parameter should cause NPE");
		}
		catch(NullPointerException expected)
		{
			// Verify that adding null didn't leave the parser in a weird state by setting the
			// correct value and using it
			otherThread.start();
			otherThread.join(EXPECTED_TEST_TIME_FOR_THIS_SUITE);
			assertThat(otherThread.isAlive()).as("otherThread took more time than what is expected for the whole test suite").isFalse();
			assertThat(failure.get()).as("Failure from otherThread" + failure.get()).isNull();
			return assertThat(parser.usage());
		}
	}

	private interface ParserInvocation<T>
	{
		void invoke(CommandLineParser parser, @Nullable T value);
	}

	@Test
	public void testThatParsedArgumentsCanBeInHashSet() throws Exception
	{
		Argument<Integer> number = integerArgument().build();
		CommandLineParser parser = CommandLineParser.withArguments(number);

		Set<ParsedArguments> parsedResults = Sets.newLinkedHashSet();
		ParsedArguments firstResult = parser.parse("1");
		parsedResults.add(firstResult);
		ParsedArguments secondResult = parser.parse("2");
		parsedResults.add(secondResult);

		assertThat(parsedResults).contains(firstResult, secondResult);
		assertThat(firstResult.hashCode()).as("Hashcode for different ParsedArguments should differ").isNotEqualTo(secondResult.hashCode());
	}

	@Test
	public void testThatEmptyNamesAreAllowed() throws Exception
	{
		assertThat(integerArgument("").parse("", "1")).isEqualTo(1);
	}

	@Test
	public void testAddingArgumentsInChainedFashion() throws ArgumentException
	{
		Argument<Integer> numberOne = integerArgument("-n").build();
		Argument<Integer> numberTwo = integerArgument("-n2").build();
		ParsedArguments result = CommandLineParser.withArguments().and(numberOne).and(numberTwo).parse("-n", "1", "-n2", "2");
		assertThat(result.get(numberOne)).isEqualTo(1);
		assertThat(result.get(numberTwo)).isEqualTo(2);
	}
}
