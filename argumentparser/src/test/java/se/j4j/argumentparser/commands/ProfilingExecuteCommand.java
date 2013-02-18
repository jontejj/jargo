package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

final class ProfilingExecuteCommand extends Command
{
	int numberOfCallsToExecute;

	@Override
	public String commandName()
	{
		return "execute";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		numberOfCallsToExecute++;
	}
}
