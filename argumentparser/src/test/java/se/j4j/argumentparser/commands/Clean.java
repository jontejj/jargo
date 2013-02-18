package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;
import se.j4j.argumentparser.commands.Build.BuildTarget;

public class Clean extends Command
{
	final BuildTarget target;

	public Clean()
	{
		this.target = new BuildTarget();
	}

	public Clean(BuildTarget target)
	{
		this.target = target;
	}

	@Override
	protected String commandName()
	{
		return "clean";
	}

	@Override
	public String description()
	{
		return "Cleans a target";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
		target.clean();
	}
}
