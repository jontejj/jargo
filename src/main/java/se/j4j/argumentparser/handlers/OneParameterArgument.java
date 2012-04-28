package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

public abstract class OneParameterArgument<T> implements ArgumentHandler<T>
{

	public abstract T parse(final @Nonnull String value) throws ArgumentException;

	@Override
	public final T parse(final @Nonnull ListIterator<String> currentArgument, final T oldValue, final @Nonnull Argument<?> argumentDefinition)
			throws ArgumentException
	{
		if(!currentArgument.hasNext())
			throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER);
		return parse(currentArgument.next());
	}

	@Override
	public String describeValue(T value)
	{
		return String.valueOf(defaultValue());
	}
}
