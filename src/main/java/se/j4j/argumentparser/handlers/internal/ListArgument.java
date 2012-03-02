package se.j4j.argumentparser.handlers.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.validators.ValueValidator;

public class ListArgument<T> implements ArgumentHandler<List<T>>
{
	final ArgumentHandler<T> argumentHandler;
	final ValueValidator<T> validator;
	final int argumentsToConsume;
	final String splitter; //TODO: make this splitter interface instead

	public static final int CONSUME_ALL = -1;

	public ListArgument(final ArgumentHandler<T> argumentHandler, final int argumentsToConsume, final ValueValidator<T> validator, final String splitter)
	{
		this.argumentHandler = argumentHandler;
		this.argumentsToConsume = argumentsToConsume;
		this.validator = validator;
		this.splitter = splitter;
	}

	public List<T> parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
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
		//TODO: use the splitter interface here
		T parsedValue = argumentHandler.parse(currentArgument, argumentDefinition);
		if(validator != null)
		{
			validator.validate(parsedValue);
		}
		return parsedValue;
	}
}