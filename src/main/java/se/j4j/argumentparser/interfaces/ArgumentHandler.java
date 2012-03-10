package se.j4j.argumentparser.interfaces;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.ArgumentFactory;
import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;

/**
 * The most powerful (but also most complex) interface to parse arguments.
 * For a simplier one use {@link StringConverter} and pass it to {@link ArgumentFactory#customArgument(StringConverter)}.
 * @author Jonatan Jönsson <jontejj@gmail.com>
 *
 * @param <T> the type this handler converts it's arguments to
 */
public interface ArgumentHandler<T>
{
	/**
	 * @param currentArgument an iterator where {@link Iterator#next()} points to the parameter for a named argument,
	 * 							for an indexed argument it points to the single unnamed argument
	 * @return the parsed value
	 * @throws ArgumentException if an error occurred while parsing the value
	 * @throws NoSuchElementException when an argument expects a parameter and it's not found
	 */
	public T parse(final @Nonnull ListIterator<String> currentArgument, final T oldValue, final @Nonnull Argument<?> argumentDefinition) throws ArgumentException;

	//TODO: add T defaultValue()

	@Nonnull
	String descriptionOfValidValues();
}
