package se.j4j.argumentparser.commands;

import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

public class InitCommand extends Command
{

	@Override
	public CommandLineParser createParserForCommandArguments()
	{
		return CommandLineParser.forArguments();
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
