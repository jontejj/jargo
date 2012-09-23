package se.j4j.argumentparser.commands;

import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

final class ProfilingCommand extends Command
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