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
package se.j4j.argumentparser.commands;

import static java.util.Collections.emptyList;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static se.j4j.argumentparser.ArgumentFactory.command;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import static se.j4j.argumentparser.utils.ExpectedTexts.expected;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder.CommandBuilder;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.ParsedArguments;
import se.j4j.argumentparser.commands.Build.BuildTarget;
import se.j4j.argumentparser.commands.CommitCommand.Commit;
import se.j4j.argumentparser.commands.CommitCommand.Repository;
import se.j4j.strings.Describers;

import com.google.common.base.Predicates;
import com.google.common.base.Suppliers;

/**
 * Tests for subclassing {@link Command}
 */
public class CommandTest
{
	/**
	 * Simulate the behavior of <a href="http://maven.apache.org/">maven</a>
	 * goals. Simple commands without any parameters.
	 */
	@Test
	public void testExecutingSeveralCommandsFromOneInvocation() throws ArgumentException
	{
		String[] arguments = {"clean", "build"};

		BuildTarget target = new BuildTarget();

		CommandLineParser.withCommands(new Build(target), new Clean(target)).parse(arguments);

		assertThat(target.isBuilt()).isTrue();
		assertThat(target.isClean()).isTrue();
	}

	@Test
	public void testMultipleCommandsEachWithSpecificArguments() throws ArgumentException
	{
		String[] logArgs = {"log", "--limit", "20"};
		String[] commitArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		Repository repo = new Repository();
		CommandLineParser parser = CommandLineParser.withCommands(new CommitCommand(repo), new LogCommand(repo));

		// LogCommand
		parser.parse(logArgs);

		assertThat(repo.logLimit).isEqualTo(20);
		assertThat(repo.commits).isEmpty();

		repo.logLimit = 10;

		// CommitCommand
		parser.parse(commitArgs);

		Commit commit = repo.commits.get(0);
		assertThat(commit.amend).isTrue();
		assertThat(commit.author).isEqualTo("jjonsson");
		assertThat(commit.files).isEqualTo(Arrays.asList(new File("A.java"), new File("B.java")));

		// Make sure that the parsing of the commit command didn't change the logLimit
		assertThat(repo.logLimit).isEqualTo(10);
	}

	/**
	 * Tests that the concept Maven has with multiple goals with the command concept from Git works
	 * in concert. Or simply put that multiple commands with command specific parameters can be
	 * given at the same time.
	 * As commit ends with a variable arity parameter (files to commit) that command has to be last.
	 */
	@Test
	public void testThatCombinedCommandsFromTheSameCommandLineBothAreExecuted() throws ArgumentException
	{
		String[] combinedInvocation = {"log", "--limit", "30", "commit", "--author=jjonsson"};
		Repository repo = new Repository();
		CommandLineParser parser = CommandLineParser.withCommands(new CommitCommand(repo), new LogCommand(repo));

		parser.parse(combinedInvocation);

		assertThat(repo.logLimit).isEqualTo(30);

		Commit commit = repo.commits.get(0);
		assertThat(commit.amend).isFalse();
		assertThat(commit.author).isEqualTo("jjonsson");
		assertThat(commit.files).isEqualTo(emptyList());
	}

	static final Argument<String> COMMIT = command(new CommitCommand(new Repository())).build();
	static final Argument<String> LOG = command(new LogCommand(new Repository())).build();

	@Test
	public void testCommandWithMissingRequiredArgument()
	{
		try
		{
			COMMIT.parse("commit");// No author
			fail("--author=??? wasn't given in the input and it should have been required");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("Missing required arguments for commit: [--author]");
		}
	}

	@Test
	public void testThatUnhandledArgumentIsCaught()
	{
		String[] args = {"log", "-verbose", "commit"};
		try
		{
			CommandLineParser.withArguments(COMMIT, LOG).parse(args);
			fail("-verbose should have been reported as an unhandled argument");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("Unexpected argument: -verbose, previous argument: log");
		}
	}

	@Test
	public void testThatRequiredArgumentsAreResetBetweenParsings() throws ArgumentException
	{
		String[] invalidArgs = {"commit"};
		String[] validArgs = {"commit", "--author=jjonsson"};

		// First make a successful parsing
		COMMIT.parse(validArgs);
		for(int i = 0; i < 2; i++)
		{
			try
			{
				// Then make sure that the previous --author didn't "stick"
				COMMIT.parse(invalidArgs);
				fail("--author=??? wasn't given in the input and it should have been required");
			}
			catch(ArgumentException expected)
			{
				assertThat(expected).hasMessage("Missing required arguments for commit: [--author]");
			}
		}
	}

	@Test
	public void testThatRepeatedParsingsWithACommandParserDoesNotAffectEachOther() throws ArgumentException
	{
		String[] firstArgs = {"commit", "--author=jjonsson"};
		String[] secondArgs = {"commit", "--author=nobody"};

		Repository repo = new Repository();
		CommandLineParser parser = CommandLineParser.withCommands(new CommitCommand(repo));

		parser.parse(firstArgs);
		parser.parse(secondArgs);

		assertThat(repo.commits.get(0).author).isEqualTo("jjonsson");
		assertThat(repo.commits.get(1).author).isEqualTo("nobody");
	}

	@Test
	public void testUsageForCommands()
	{
		BuildTarget target = new BuildTarget();
		CommandLineParser parser = CommandLineParser.withCommands(new Build(target), new Clean(target), new CommitCommand(new Repository()));
		String usage = parser.usage();
		assertThat(usage).isEqualTo(expected("commandsWithArguments"));
	}

	private static final class CommandWithIndexedArguments extends Command
	{
		CommandWithIndexedArguments()
		{
			super(integerArgument().arity(2).build());
		}

		@Override
		protected String commandName()
		{
			return "aCommand";
		}

		@Override
		protected void execute(ParsedArguments parsedArguments)
		{
		}
	}

	@Test
	public void testCommandWithMissingIndexedArgument()
	{
		Argument<String> command = command(new CommandWithIndexedArguments()).build();
		try
		{
			command.parse("aCommand", "1");
			fail("Indexed argument should require two parameters");
		}
		catch(ArgumentException missingSecondParameterForIndexedArgument)
		{
			assertThat(missingSecondParameterForIndexedArgument).hasMessage("Missing second <integer> parameter for aCommand");
		}
	}

	@Test
	public void testThatTheInnerMostCommandIsPrintedInErrorMessage()
	{
		Argument<String> superCommand = command(new Command(command(new CommandWithIndexedArguments()).build()){

			@Override
			protected String commandName()
			{
				return "superCommand";
			}

			@Override
			protected void execute(ParsedArguments parsedArguments)
			{
			}
		}).build();

		try
		{
			superCommand.parse("superCommand", "aCommand", "1");
			fail("Indexed argument should require two parameters");
		}
		catch(ArgumentException missingSecondParameterForIndexedArgument)
		{
			assertThat(missingSecondParameterForIndexedArgument).hasMessage("Missing second <integer> parameter for aCommand");
		}
	}

	private static final class CommandWithSubCommand extends Command
	{
		private static final Argument<Integer> number = integerArgument("-n").build();

		public CommandWithSubCommand()
		{
			super(command(new Command(number){

				@Override
				public String description()
				{
					return "A subcommand with an argument";
				}

				@Override
				protected void execute(ParsedArguments subCommandArgs)
				{
					assertThat(subCommandArgs.get(number)).isEqualTo(1);
				}

				@Override
				protected String commandName()
				{
					return "subcommand";
				}
			}).build());
		}

		@Override
		protected void execute(ParsedArguments commandArgs)
		{
		}

		@Override
		protected String commandName()
		{
			return "command";
		}
	}

	/**
	 * Tests that commands within commands works
	 */
	@Test
	public void testSubCommand() throws ArgumentException
	{
		Argument<String> commandWithSubCommand = command(new CommandWithSubCommand()).build();

		commandWithSubCommand.parse("command", "subcommand", "-n", "1");

		assertThat(commandWithSubCommand.usage()).isEqualTo(expected("commandWithSubCommand"));
	}

	/**
	 * Tests several commands that each have their own indexed arguments
	 */
	@Test
	public void testMultipleCommandEachWithIndexedArguments() throws ArgumentException
	{
		CommandLineParser parser = CommandLineParser.withCommands(	new CommandWithOneIndexedArgument(), new CommandWithTwoIndexedArguments(),
																	new CommandWithThreeIndexedArguments());

		parser.parse("one_arg", "1", "two_args", "1", "2", "three_args", "1", "2", "3");
	}

	@Test
	public void testThatCorrectCommandIsMentionedInErrorMessage()
	{
		CommandLineParser parser = CommandLineParser.withCommands(	new CommandWithOneIndexedArgument(), new CommandWithTwoIndexedArguments(),
																	new CommandWithThreeIndexedArguments());
		try
		{
			// Switched order of two_args and three_args for extra test harness
			parser.parse("one_arg", "1", "three_args", "1", "2", "3", "two_args", "1");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("Missing second <integer> parameter for two_args");
		}
	}

	@Test
	public void testThatParserForCommandArgumentsIsOnlyCreatedWhenCommandIsExecuted() throws ArgumentException
	{
		ProfilingCommand profiler = new ProfilingCommand();
		Argument<String> command = command(profiler).build();

		try
		{
			command.parse("profile");
			fail("Invalid arguments passed to super constructor in ProfilingCommand should cause a lazy initialization error");
		}
		catch(IllegalArgumentException expected)
		{
			assertThat(expected).hasMessage("-n is handled by several arguments");
		}
	}

	@Test
	public void testThatCommandIsExecutedOnlyOnce() throws ArgumentException
	{
		ProfilingExecuteCommand profiler = new ProfilingExecuteCommand();
		Argument<String> command = command(profiler).build();

		assertThat(profiler.numberOfCallsToExecute).isZero();
		command.parse("execute");
		assertThat(profiler.numberOfCallsToExecute).isEqualTo(1);
	}

	@Test
	public void testRepeatedCommands() throws ArgumentException
	{
		assertThat(command(new Clean()).repeated().parse("clean", "clean", "clean")).hasSize(3);
	}

	@Test
	public void testThatParsedArgumentsCanBeQueriedForTheUsedCommandName() throws ArgumentException
	{
		Argument<String> command = command(new Clean()).ignoreCase().build();
		ParsedArguments result = CommandLineParser.withArguments(command).parse("Clean");

		assertThat(result.get(command)).isEqualTo("Clean");
	}

	@Test
	public void testAddingCommandsInChainedFashion() throws ArgumentException
	{
		BuildTarget target = new BuildTarget();
		CommandLineParser.withArguments().and(new Clean(target)).and(new Build(target)).parse("clean", "build");
		assertThat(target.isClean()).isTrue();
		assertThat(target.isBuilt()).isTrue();
	}

	@Test
	public void testThatDescriptionForCommandIsLazilyCreated() throws ArgumentException
	{
		FailInDescription failDescription = new FailInDescription();
		command(failDescription).parse("fail_description");
	}

	private static final class FailInDescription extends Command
	{
		@Override
		public String commandName()
		{
			return "fail_description";
		}

		@Override
		protected void execute(ParsedArguments parsedArguments)
		{
		}

		@Override
		public String description()
		{
			fail("Description should only be called if usage is printed");
			return "Unreachable description";
		}
	}

	// This is what's tested
	@SuppressWarnings("deprecation")
	@Test
	public void testThatInvalidArgumentPropertiesOnCommandIsDeprecated()
	{
		CommandBuilder builder = command(new Build());
		try
		{
			builder.arity(2);
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.defaultValue("");
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.defaultValueDescriber(Describers.<String>toStringDescriber());
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.defaultValueSupplier(Suppliers.ofInstance(""));
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.defaultValueDescription("");
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.limitTo(Predicates.<String>alwaysFalse());
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.required();
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.splitWith("-");
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
		try
		{
			builder.variableArity();
			fail("method should throw as it's deprecated");
		}
		catch(IllegalStateException expected)
		{
		}
	}
}