package se.j4j.argumentparser.commands;

import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.commands.CommitCommand.Repository;

import com.google.common.collect.Lists;

public class LogCommand extends Command
{
	public final Repository repository;

	private static final Argument<Integer> LIMIT = integerArgument("--limit", "-l").build();

	public LogCommand(final Repository repo)
	{
		repository = repo;
	}

	@Override
	protected String commandName()
	{
		return "log";
	}

	@Override
	protected List<Argument<?>> commandArguments()
	{
		List<Argument<?>> arguments = Lists.newArrayList();
		arguments.add(LIMIT);
		return arguments;
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		repository.logLimit = parsedArguments.get(LIMIT);
	}
}
