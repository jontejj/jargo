package se.j4j.argumentparser.commands;

import static se.j4j.argumentparser.ArgumentFactory.fileArgument;
import static se.j4j.argumentparser.ArgumentFactory.optionArgument;
import static se.j4j.argumentparser.ArgumentFactory.stringArgument;

import java.io.File;
import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParsers;
import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;

import com.google.common.collect.Lists;

public class CommitCommand extends Command
{
	public final Repository repository;

	private static Argument<Boolean> AMEND = optionArgument("--amend").build();
	private static Argument<String> AUTHOR = stringArgument("--author").required().separator("=").build();
	private static Argument<List<File>> FILES = fileArgument().consumeAll().build();

	public CommitCommand(final Repository repo)
	{
		repository = repo;
	}

	@Override
	public CommandLineParser createParserForCommandArguments()
	{
		return CommandLineParsers.forArguments(AMEND, AUTHOR, FILES);
	}

	@Override
	protected void execute(final ParsedArguments parsedArguments)
	{
		repository.commits.add(new Commit(parsedArguments));
	}

	@Override
	public String commandName()
	{
		return "commit";
	}

	public static class Repository
	{
		List<Commit> commits = Lists.newArrayList();
	}

	public static class Commit
	{
		final List<File> files;
		final boolean amend;
		final String author;

		public Commit(final ParsedArguments arguments)
		{
			amend = arguments.get(AMEND);
			files = arguments.get(FILES);
			author = arguments.get(AUTHOR);
		}
	}
}
