package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParsers;
import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;
import se.j4j.argumentparser.Command;

public class InitCommand extends Command
{

	@Override
	public CommandLineParser createParserForCommandArguments()
	{
		return CommandLineParsers.forArguments();
	}

	@Override
	public void execute(final ParsedArguments parsedArguments)
	{
		// Here would the init code be
	}

	@Override
	public String commandName()
	{
		return "init";
	}

}
