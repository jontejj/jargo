package se.j4j.argumentparser.handlers.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.interfaces.ValueValidator;

public class ListArgument<T> implements ArgumentHandler<List<T>>
{
	final ArgumentHandler<T> argumentHandler;
	final ValueValidator<T> validator;
	final int argumentsToConsume;

	public static final int CONSUME_ALL = -1;

	public ListArgument(final ArgumentHandler<T> argumentHandler, final int argumentsToConsume, final ValueValidator<T> validator)
	{
		this.argumentHandler = argumentHandler;
		this.argumentsToConsume = argumentsToConsume;
		this.validator = validator;
	}

	@Override
	public List<T> parse(final ListIterator<String> currentArgument, final List<T> list, final Argument<?> argumentDefinition) throws ArgumentException
	{
		if(list != null)
		{
			throw UnhandledRepeatedArgument.create(argumentDefinition);
		}
		List<T> parsedArguments = new ArrayList<T>();
		if(argumentsToConsume == CONSUME_ALL)
		{
			while(currentArgument.hasNext())
			{
				parsedArguments.add(parseValue(currentArgument, argumentDefinition));
			}
		}
		else
		{
			for(int i = 0;i<argumentsToConsume; i++)
			{
				parsedArguments.add(parseValue(currentArgument, argumentDefinition));
			}
		}
		return parsedArguments;
	}

	private T parseValue(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
		T parsedValue = argumentHandler.parse(currentArgument, null, argumentDefinition);
		if(validator != null)
		{
			validator.validate(parsedValue);
		}
		return parsedValue;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return argumentsToConsume + " of " + argumentHandler.descriptionOfValidValues();
	}
}