package se.j4j.argumentparser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


public class ListArgument<T> implements ArgumentHandler<List<T>>
{
	final ArgumentHandler<T> argumentHandler;
	int argumentsToConsume = CONSUME_ALL;

	static final int CONSUME_ALL = -1;

	ListArgument(final ArgumentHandler<T> argumentHandler, final int argumentsToConsume)
	{
		this.argumentHandler = argumentHandler;
		this.argumentsToConsume = argumentsToConsume;
	}

	public List<T> parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		List<T> parsedArguments = new ArrayList<T>();
		if(argumentsToConsume == CONSUME_ALL)
		{
			while(currentArgument.hasNext())
			{
				parsedArguments.add(argumentHandler.parse(currentArgument));
			}
		}
		else
		{
			for(int i = 0;i<argumentsToConsume; i++)
			{
				parsedArguments.add(argumentHandler.parse(currentArgument));
			}
		}
		return parsedArguments;
	}
}
