package se.j4j.argumentparser.commands;

import static java.util.Collections.emptyList;
import static junit.framework.Assert.fail;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.command;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.ArgumentFactory;
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

	@Test(expected = MissingRequiredArgumentException.class)
	public void testCommandWithMissingRequiredArgument() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "A.java", "B.java"}; // No author

		CommandLineParser.forArguments(COMMIT).parse(args);
	}

	@Test(expected = UnexpectedArgumentException.class)
	public void testThatUnhandledVerboseArgumentIsCaught() throws ArgumentException
	{
		String[] args = {"-verbose", "log", "commit", "--amend", "A.java", "B.java"};

		CommandLineParser.forArguments(COMMIT, LOG).parse(args);
	}

	@Test
	public void testMultipleCommands() throws ArgumentException
	{
		String[] logArgs = {"log", "--limit", "20"};
		String[] commitArgs = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};
		CommitCommand commitCommand = new CommitCommand(new Repository());
		LogCommand logCommand = new LogCommand(new Repository());
		CommandLineParser parser = CommandLineParser.forCommands(commitCommand, logCommand);

		parser.parse(logArgs);

		assertThat(logCommand.repository.logLimit).isEqualTo(20);
		assertThat(commitCommand.repository.commits).isEmpty();

		logCommand.repository.logLimit = 10;

		parser.parse(commitArgs);

		Commit commit = commitCommand.repository.commits.get(0);
		assertThat(commit.amend).isTrue();
		assertThat(commit.author).isEqualTo("jjonsson");
		assertThat(commit.files).isEqualTo(Arrays.asList(new File("A.java"), new File("B.java")));

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
		CommitCommand commitCommand = new CommitCommand(new Repository());
		LogCommand logCommand = new LogCommand(new Repository());
		CommandLineParser parser = CommandLineParser.forCommands(commitCommand, logCommand);

		parser.parse(combinedInvocation);

		assertThat(logCommand.repository.logLimit).isEqualTo(30);

		Commit commit = commitCommand.repository.commits.get(0);
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

		CommandLineParser parser = CommandLineParser.forArguments(COMMIT);
		// First make a successful parsing
		parser.parse(validArgs);
		for(int i = 0; i < 2; i++)
		{
			try
			{
				parser.parse(invalidArgs);
				fail("--author=??? wasn't given in the input and it should have been required");
			}
			catch(MissingRequiredArgumentException expected)
			{
			}
		}
	}

	@Test
	public void testThatRepeatedParsingsWithACommandParserWorks() throws ArgumentException
	{
		String[] args = {"commit", "--amend", "--author=jjonsson", "A.java", "B.java"};

		Repository repo = new Repository();
		Argument<String> commitCommand = command(new CommitCommand(repo)).build();

		CommandLineParser parser = CommandLineParser.forArguments(commitCommand);

		for(int i = 0; i < 2; i++)
		{
			parser.parse(args);
			Commit commit = repo.commits.get(i);
			assertThat(commit.amend).isTrue();
			assertThat(commit.author).isEqualTo("jjonsson");
			assertThat(commit.files).isEqualTo(Arrays.asList(new File("A.java"), new File("B.java")));
		}
	}

	@Test
	public void testUsageForCommands()
	{
		BuildTarget target = new BuildTarget();
		Argument<String> buildCommand = ArgumentFactory.command(new Build(target)).description("Builds a target").build();
		Argument<String> cleanCommand = ArgumentFactory.command(new Clean(target)).description("Cleans a target").build();
		Argument<String> commitCommand = command(new CommitCommand(new Repository())).build();

		String usage = CommandLineParser.forArguments(buildCommand, cleanCommand, commitCommand).usage("CommandUsage");
		// TODO: fix and assert
		System.out.println(usage);
	}

	@Test
	public void testThatToStringReturnsCommandName()
	{
		Command command = new Build();
		assertThat(command.toString()).isEqualTo(command.commandName());
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
}
