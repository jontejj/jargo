package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

public abstract class NoParameterArgument<T> implements ArgumentHandler<T>
{
	public abstract T get() throws ArgumentException;

	@Override
	public final T parse(final @Nonnull ListIterator<String> currentArgument, final T oldValue, final @Nonnull Argument<?> argumentDefinition)
			throws ArgumentException
	{
		return get();
	}

	/**
	 * Only the existence of the flag matters, no specific value.
	 * Use {@link ArgumentBuilder#description} to describe this argument.
	 */
	@Override
	public String descriptionOfValidValues()
	{
		return "";
	}
}
