package se.j4j.argumentparser;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentFactory.DefaultArgumentBuilder;
import se.j4j.argumentparser.exceptions.ArgumentException;

/**
 * <pre>
 * This interface makes it possible to convert two (or more) {@link String}s into a single <code>T</code> value.
 * For a simpler one use {@link StringConverter}.
 * 
 * Pass the constructed {@link ArgumentHandler} to {@link DefaultArgumentBuilder#DefaultArgumentBuilder(ArgumentHandler)} 
 * to create an {@link Argument} instance using it.
 * 
 * @param <T> the type this handler converts it's arguments to
 * </pre>
 */
@Immutable
public interface ArgumentHandler<T>
{
	/**
	 * @param currentArgument an iterator where {@link Iterator#next()} points
	 *            to the parameter for a named argument,
	 *            for an indexed argument it points to the single unnamed
	 *            argument
	 * @return the parsed value
	 * @throws ArgumentException if an error occurred while parsing the value
	 * @throws NoSuchElementException when an argument expects a parameter and
	 *             it's not found
	 */
	T parse(@Nonnull final ListIterator<String> currentArgument, @Nullable final T oldValue, @Nonnull final Argument<?> argumentDefinition)
			throws ArgumentException;

	/**
	 * Describes what values this {@link ArgumentHandler} accepts
	 * 
	 * @return a description string to show in usage texts
	 */
	@Nonnull
	String descriptionOfValidValues();

	/**
	 * If you can provide a sample value do so, it will look much better
	 * in the usage texts, if not return null
	 * 
	 * @return
	 */
	@Nullable
	T defaultValue();

	/**
	 * @return <code>value</code> described, or null if no description is needed
	 */
	@Nullable
	String describeValue(@Nullable T value);
}
