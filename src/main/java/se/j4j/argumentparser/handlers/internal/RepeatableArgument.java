package se.j4j.argumentparser.handlers.internal;

import java.util.ListIterator;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

/**
 * Used when you want to handle old values in a certain way
 * @param <T> the type the repeated values are stored in, may be a List or Map for example
 */
public interface RepeatableArgument<T>
{
	public T parseRepeated(final ListIterator<String> currentArgument, final T oldValue, final Argument<?> argumentDefinition) throws ArgumentException;
}
