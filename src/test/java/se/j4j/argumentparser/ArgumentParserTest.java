package se.j4j.argumentparser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static se.j4j.argumentparser.ArgumentFactory.booleanArgument;
import static se.j4j.argumentparser.ArgumentFactory.commandArgument;
import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.ArgumentFactory.integerArithmeticArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public class ArgumentParserTest
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
	@Test(expected = ArgumentException.class)
	public void testUnhandledParameter() throws ArgumentException
	{
		String[] args = {"Unhandled"};
		ArgumentParser.forArguments().throwOnUnexpectedArgument().parse(args);
	}

	@Test
	public void testUnhandledParameterWithoutThrow() throws ArgumentException
	{
		String[] args = {"Unhandled"};
		assertNotNull(ArgumentParser.forArguments().parse(args));
	}

	@Test
	public void testArithmeticArgumentAddOperation() throws ArgumentException
	{
		String[] args = {"--sum-elements", "5", "6", "-3"};

		Argument<Integer> sum = integerArithmeticArgument("--sum-elements").operation('+').build();
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

	@Test
	public void testTwoParametersForNamedArgumentRepeated() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};

		Argument<List<List<Integer>>> numbers = integerArgument("--numbers").arity(2).repeated().build();

		ParsedArguments parsed = ArgumentParser.forArguments(numbers).parse(args);

		List<List<Integer>> numberLists = new ArrayList<List<Integer>>();
		numberLists.add(Arrays.asList(5, 6));
		numberLists.add(Arrays.asList(3, 4));
		List<List<Integer>> actual = parsed.get(numbers);
		assertEqual("", numberLists, actual);
	}

	@Test
	public void testTwoParametersForNamedArgumentRepeatedSingle() throws ArgumentException
	{
		String[] args = {"--number", "1", "--number", "2"};

		Argument<List<Integer>> number = integerArgument("--number").repeated().build();

		ParsedArguments parsed = ArgumentParser.forArguments(number).parse(Arrays.asList(args));

		assertEquals(Arrays.asList(1, 2), parsed.get(number));
	}

	@Test(expected = UnhandledRepeatedArgument.class)
	public void testTwoParametersForNamedArgumentRepeatedNotAllowed() throws ArgumentException
	{
		String[] args = {"--numbers", "5", "6", "--numbers", "3", "4"};

		Argument<List<Integer>> numbers = integerArgument("--numbers").arity(2).build();

		ArgumentParser.forArguments(numbers).parse(args);
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

	@Test
	public void testIgnoringCaseCombinedWithSeperator() throws ArgumentException
	{
		Argument<String> logLevel = stringArgument("-log").ignoreCase().separator("=").build();

		ArgumentParser parser = ArgumentParser.forArguments(logLevel);

		assertEqual("wrong log level", "debug", parser.parse("-Log=debug").get(logLevel));
		assertEqual("wrong log level", "debug", parser.parse("-log=debug").get(logLevel));
	}

	@Test
	public void testIgnoringCaseCombinedWithAlphaSeperator() throws ArgumentException
	{
		Argument<String> logLevel = stringArgument("-log").ignoreCase().separator("A").build();

		ArgumentParser parser = ArgumentParser.forArguments(logLevel);

		assertEqual("wrong log level", "debug", parser.parse("-LogAdebug").get(logLevel));
		assertEqual("wrong log level", "debug", parser.parse("-logAdebug").get(logLevel));
	}

	@Test(expected = IllegalStateException.class)
	public void testCallingRepeatedBeforeArity()
	{
		integerArgument("--number").repeated().arity(2);
	}

	@Test(expected = IllegalStateException.class)
	public void testCallingRepeatedBeforeConsumeAll()
	{
		integerArgument("--number").repeated().consumeAll();
	}

	@Test(expected = IllegalStateException.class)
	public void testMakingAnOptionalArgumentRequired()
	{
		optionArgument("-l").required();
	}

	@Test(expected = IllegalStateException.class)
	public void testSettingADefaultValueForARequiredArgument()
	{
		integerArgument("-l").required().defaultValue(42);
	}

	@Test(expected = IllegalStateException.class)
	public void testMakingARequiredArgumentWithDefaultValue()
	{
		integerArgument("-l").defaultValue(42).required();
	}

	@Test
	public void testMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {};

		Argument<Integer> number = integerArgument("--number").required().build();
		Argument<Integer> number2 = integerArgument("--number2").required().build();

		try
		{
			ParsedArguments parsed = ArgumentParser.forArguments(number, number2).parse(args);
			fail("Required argument silently ignored. Parsed data: " + parsed);
		}
		catch(MissingRequiredArgumentException ex)
		{
			assertEquals("", setOf(number, number2), ex.missingArguments());
		}

	}

	/**
	 *
	 * @Parameters(separators = "=", commandDescription = "Record changes to the repository")
public class CommandCommit {

  @Parameter(description = "The list of files to commit")
  public List<String> files;

  @Parameter(names = "--amend", description = "Amend")
  public Boolean amend = false;

  @Parameter(names = "--author")
  public String author;
}

	 * CommandMain cm = new CommandMain();
		JCommander jc = new JCommander(cm);

		CommandAdd add = new CommandAdd();
		jc.addCommand("add", add);
		CommandCommit commit = new CommandCommit();
		jc.addCommand("commit", commit);

		jc.parse("-v", "commit", "--amend", "--author=cbeust", "A.java", "B.java");

		Assert.assertTrue(cm.verbose);
		Assert.assertEquals(jc.getParsedCommand(), "commit");
		Assert.assertTrue(commit.amend);
		Assert.assertEquals(commit.author, "cbeust");
		Assert.assertEquals(commit.files, Arrays.asList("A.java", "B.java"));

	 */

	@Test
	public void testComparisonToJCommander() throws ArgumentException
	{
		String[] args = {"-v", "commit", "--amend", "--author=cbeust", "A.java", "B.java"};

		Argument<Boolean> amend = optionArgument("--amend").build();
		Argument<String> author = stringArgument("author").separator("=").build();
		Argument<List<File>> files = fileArgument().consumeAll().build();

		CommandParser commit = commandArgument("commit").withArguments(amend, author, files).build();

		ArgumentParser.forArguments(commit).parse(args);

		assertTrue(commit.get(amend));
	}

	@Test
	public void testInterfaceBasedCommandExecuting() throws ArgumentException
	{
		String[] args = {"-v", "commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		for(int i = 0; i < 2; i++)
		{
			CommitCommand commit = new CommitCommand();
			ArgumentParser.forArguments(commit.getCommand()).parse(args);

			assertTrue(commit.didExecute());
		}
	}

	@Test
	public void testInterfaceBasedCommandExecutingFailed() throws ArgumentException
	{
		String[] args = {"-v", "commit", "--amend", "A.java", "B.java"};

		for(int i = 0; i < 2; i++)
		{
			CommitCommand commit = new CommitCommand();

			ArgumentParser.forArguments(commit.getCommand()).parse(args);
			assertTrue(commit.didFail());
			assertFalse(commit.didExecute());
		}
	}

	@Test
	public void testInterfaceBasedCommandExecutingMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {"-v", "commit", "--amend", "A.java", "B.java"}; //No author

		CommitCommand commit = new CommitCommand();

		ArgumentParser.forArguments(commit.getCommand()).parse(args);

		assertTrue(commit.getExceptionThatCausedTheFailure() instanceof MissingRequiredArgumentException);
	}

	@Test
	public void testHostPort() throws ArgumentException
	{
		String[] args = {"-target" , "example.com:8080"};

		Argument<HostPort> hostPort = new ArgumentBuilder<HostPort>(new HostPortArgument()).names("-target").build();

		ParsedArguments parsed = ArgumentParser.forArguments(hostPort).parse(args);

		assertEquals("example.com", parsed.get(hostPort).host);
		assertEquals(8080, parsed.get(hostPort).port);
	}

	//TODO: add support for @/path/to/arguments

	private static <T> Set<T> setOf(final T first, final T second)
	{
		Set<T> result = new HashSet<T>(2);
		result.add(first);
		result.add(second);
		return result;
	}

	private static <T> void assertEqual(final String message, final T expected, final T actual)
	{
		assertEquals(message, expected, actual);
	}
}
