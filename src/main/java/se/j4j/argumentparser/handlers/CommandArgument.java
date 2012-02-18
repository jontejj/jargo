package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public abstract class CommandArgument implements ArgumentHandler<String>
{
	public abstract ArgumentParser getParserInstance();

	public abstract void handle(ParsedArguments parsedArguments);

	public String parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
		ParsedArguments result = getParserInstance().parse(currentArgument);
		handle(result);
		return "handled"; //Can be used to check for the existence of this argument in the given input arguments
	}

	@Override
	public String toString()
	{
		return getParserInstance().toString();
	}

}
