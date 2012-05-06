package se.j4j.argumentparser.CustomCommands;

import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.CommandArgument;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public class InitCommand extends CommandArgument
{

	@Override
	public ArgumentParser createParserInstance()
	{
		return ArgumentParser.forArguments();
	}

	@Override
	public void handle(final ParsedArguments parsedArguments)
	{
		// Here would the init code be
	}

	@Override
	public String commandName()
	{
		return "init";
	}

}
