package se.j4j.argumentparser.handlers.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class ListArgument<T> implements ArgumentHandler<List<T>>
{
	final ArgumentHandler<T> argumentHandler;
	final int argumentsToConsume;

	public static final int CONSUME_ALL = -1;

	public ListArgument(final ArgumentHandler<T> argumentHandler, final int argumentsToConsume)
	{
		this.argumentHandler = argumentHandler;
		this.argumentsToConsume = argumentsToConsume;
	}

	public List<T> parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
		List<T> parsedArguments = new ArrayList<T>();
		if(argumentsToConsume == CONSUME_ALL)
		{
			while(currentArgument.hasNext())
			{
				parsedArguments.add(argumentHandler.parse(currentArgument, argumentDefinition));
			}
		}
		else
		{
			for(int i = 0;i<argumentsToConsume; i++)
			{
				parsedArguments.add(argumentHandler.parse(currentArgument, argumentDefinition));
			}
		}
		return parsedArguments;
	}
}