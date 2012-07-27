package se.j4j.argumentparser.commands;

import static java.util.Collections.emptyList;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.command;
import static se.j4j.argumentparser.utils.UsageTexts.expected;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.commands.Build.BuildTarget;
import se.j4j.argumentparser.commands.CommitCommand.Commit;
import se.j4j.argumentparser.commands.CommitCommand.Repository;

public class TestCommands
{
	static final Argument<String> COMMIT = command(new CommitCommand(new Repository())).build();
	static final Argument<String> LOG = command(new LogCommand(new Repository())).build();

	@Test
	public void testCommandWithMissingRequiredArgument()
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"}; // No author
		try
		{
			COMMIT.parse(args);
			fail("--author=??? wasn't given in the input and it should have been required");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("Missing required arguments: [--author]");
		}
	}

	@Test
	public void testThatUnhandledVerboseArgumentIsCaught()
	{
		String[] args = {"-verbose", "log", "commit", "--amend", "A.java", "B.java"};
		try
		{
			CommandLineParser.forArguments(COMMIT, LOG).parse(args);
			fail("-verbose should have been reported as an unhandled argument");
		}
		catch(ArgumentException expected)
		{
			assertThat(expected).hasMessage("Unexpected argument: -verbose");
		}
	}

	@Test
	public void testMultipleCommands() throws ArgumentException
	{
		String[] logArgs = {"log", "--limit", "20"};
		String[] commitArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};
		CommitCommand commitCommand = new CommitCommand(new Repository());
		LogCommand logCommand = new LogCommand(new Repository());
		CommandLineParser parser = CommandLineParser.forCommands(commitCommand, logCommand);

		// LogCommand
		parser.parse(logArgs);

		assertThat(logCommand.repository.logLimit).isEqualTo(20);
		assertThat(commitCommand.repository.commits).isEmpty();

		logCommand.repository.logLimit = 10;

		// CommitCommand
		parser.parse(commitArgs);

		Commit commit = commitCommand.repository.commits.get(0);
		assertThat(commit.amend).isTrue();
		assertThat(commit.author).isEqualTo("jjonsson");
		assertThat(commit.files).isEqualTo(Arrays.asList(new File("A.java"), new File("B.java")));

		// Make sure that the parsing of the commit command didn't change the logLimit
		assertThat(logCommand.repository.logLimit).isEqualTo(10);
	}

	/**
	 * Tests that the concept Maven has with multiple goals with the command concept from Git works
	 * in concert.
	 * Or simply put that multiple commands with command specific parameters can be given at the
	 * same time.
	 * As commit ends with a variable arity parameter (files to commit) that command has to be last.
	 */
	@Test
	public void testThatMultipleCommandsFromTheSameCommandLineAreExecuted() throws ArgumentException
	{
		String[] combinedInvocation = {"log", "--limit", "30", "commit", "--author=jjonsson"};
		Repository repo = new Repository();
		CommandLineParser parser = CommandLineParser.forCommands(new CommitCommand(repo), new LogCommand(repo));

		parser.parse(combinedInvocation);

		assertThat(repo.logLimit).isEqualTo(30);

		Commit commit = repo.commits.get(0);
		assertThat(commit.amend).isFalse();
		assertThat(commit.author).isEqualTo("jjonsson");
		assertThat(commit.files).isEqualTo(emptyList());
	}

	/**
	 * Simulate the behavior of <a href="http://maven.apache.org/">maven</a>
	 * goals. Simple commands without any parameters.
	 */
	@Test
	public void testExecutingSeveralCommandsFromOneInvocation() throws ArgumentException
	{
		String[] arguments = {"clean", "build"};

		BuildTarget target = new BuildTarget();

		CommandLineParser.forCommands(new Build(target), new Clean(target)).parse(arguments);

		assertThat(target.isBuilt()).isTrue();
		assertThat(target.isClean()).isTrue();

	}

	@Test
	public void testThatRequiredArgumentsAreResetBetweenParsings() throws ArgumentException
	{
		String[] invalidArgs = {"commit", "--amend", "A.java", "B.java"};
		String[] validArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

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
				assertThat(expected).hasMessage("Missing required arguments: [--author]");
			}
		}
	}

	@Test
	public void testThatRepeatedParsingsWithACommandParserDoesNotAffectEachOther() throws ArgumentException
	{
		String[] firstArgs = {"commit", "--author=jjonsson"};
		String[] secondArgs = {"commit", "--author=nobody"};

		Repository repo = new Repository();
		CommandLineParser parser = CommandLineParser.forCommands(new CommitCommand(repo));

		parser.parse(firstArgs);
		parser.parse(secondArgs);

		assertThat(repo.commits.get(0).author).isEqualTo("jjonsson");
		assertThat(repo.commits.get(1).author).isEqualTo("nobody");
	}

	/**
	 * TODO: fix usage for commands
	 */
	@Test
	@Ignore
	public void testUsageForCommands()
	{
		BuildTarget target = new BuildTarget();
		CommandLineParser parser = CommandLineParser.forCommands(new Build(target), new Clean(target), new CommitCommand(new Repository()));
		String usage = parser.usage("CommandUsage");
		assertThat(usage).isEqualTo(expected("commandsWithArguments"));
	}

	@Test
	public void testThatParserForCommandArgumentsIsOnlyCreatedWhenCommandIsExecuted() throws ArgumentException
	{
		ProfilingCommand profiler = new ProfilingCommand();
		Argument<String> command = command(profiler).build();

		assertThat(ProfilingCommand.numberOfCallsToCreate).isZero();
		command.parse("profile");
		assertThat(command.usage("CallUsageToEnsureUsageUsesTheSameParserAsParseUsed")).isNotEmpty();

		assertThat(ProfilingCommand.numberOfCallsToCreate).isEqualTo(1);
	}

	private static final class ProfilingCommand extends Command
	{
		static int numberOfCallsToCreate = 0;

		@Override
		protected List<Argument<?>> commandArguments()
		{
			numberOfCallsToCreate++;
			return super.commandArguments();
		}

		@Override
		public String commandName()
		{
			return "profile";
		}

		@Override
		protected void execute(ParsedArguments parsedArguments)
		{
		}
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
}
