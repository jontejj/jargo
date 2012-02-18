package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;

public abstract class OneParameterArgument<T> implements ArgumentHandler<T>
{

	public abstract T parse(final String value) throws ArgumentException;

	public final T parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition) throws ArgumentException
	{
		if(!currentArgument.hasNext())
		{
			throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER).errorneousArgument(argumentDefinition);
		}
		return parse(currentArgument.next());
	}
}
