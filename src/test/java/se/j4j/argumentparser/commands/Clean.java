package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.commands.Build.BuildTarget;

public class Clean extends Command
{
	final BuildTarget target;

	Clean(BuildTarget target)
	{
		this.target = target;
	}

	@Override
	public String commandName()
	{
		return "clean";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		target.clean();
	}
}