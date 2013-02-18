package se.j4j.argumentparser.commands;

import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;
import se.j4j.argumentparser.commands.CommitCommand.Repository;

public class LogCommand extends Command
{
	public final Repository repository;

	private static final Argument<Integer> LIMIT = integerArgument("--limit", "-l").build();

	public LogCommand(final Repository repo)
	{
		super(LIMIT);
		repository = repo;
	}

	@Override
	protected String commandName()
	{
		return "log";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		repository.logLimit = parsedArguments.get(LIMIT);
	}
}
