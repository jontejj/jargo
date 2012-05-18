package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParsers;
import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;
import se.j4j.argumentparser.commands.Build.BuildTarget;

public class Clean extends Command
{
	final BuildTarget target;

	Clean(BuildTarget target)
	{
		this.target = target;
	}

	@Override
	protected CommandLineParser createParserForCommandArguments()
	{
		return CommandLineParsers.forAnyArguments();
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
