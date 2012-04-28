package se.j4j.argumentparser.handlers.internal;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.utils.ListUtil;

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

	@Override
	public List<T> parse(final ListIterator<String> currentArgument, final List<T> list, final Argument<?> argumentDefinition)
			throws ArgumentException
	{
		if(list != null)
			throw UnhandledRepeatedArgument.create(argumentDefinition);

		// TODO: fetch the actual value from currentArgument instead of 10
		int expectedSize = argumentsToConsume == CONSUME_ALL ? 10 : argumentsToConsume;
		List<T> parsedArguments = new ArrayList<T>(expectedSize);
		if(argumentsToConsume == CONSUME_ALL)
		{
			while(currentArgument.hasNext())
			{
				parsedArguments.add(parseValue(currentArgument, argumentDefinition));
			}
		}
		else
		{
			for(int i = 0; i < argumentsToConsume; i++)
			{
				parsedArguments.add(parseValue(currentArgument, argumentDefinition));
			}
		}
		return parsedArguments;
	}

	private T parseValue(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
		T parsedValue = argumentHandler.parse(currentArgument, null, argumentDefinition);
		return parsedValue;
	}

	@Override
	public String descriptionOfValidValues()
	{
		// TODO: print meta descriptions
		return argumentsToConsume + " of " + argumentHandler.descriptionOfValidValues();
	}

	@Override
	public List<T> defaultValue()
	{
		return emptyList();
	}

	@Override
	public String describeValue(List<T> value)
	{
		return ListUtil.describeList(value);
	}
}
