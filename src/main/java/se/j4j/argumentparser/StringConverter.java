package se.j4j.argumentparser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import se.j4j.argumentparser.exceptions.ArgumentException;

/**
 * Pass your created {@link StringConverter}s to
 * {@link ArgumentFactory#customArgument(StringConverter)} to create an
 * {@link Argument} that uses it to parse values.
 * 
 * @param <T> the type to convert {@link String}s to
 */
public interface StringConverter<T>
{
	/**
	 * Converts the given {@link String} to the type <code>T</code>
	 * 
	 * @param argument the string as given from the command line
	 * @return
	 * @throws ArgumentException if the string isn't valid according to
	 *             {@link #descriptionOfValidValues()}
	 */
	T convert(String argument) throws ArgumentException;

	/**
	 * Describes what values this {@link StringConverter} accepts
	 * 
	 * @return a description string to show in usage texts
	 */
	@Nonnull
	String descriptionOfValidValues();

	/**
	 * If you can provide a default value do so, it will look much better
	 * in the usage texts, if not return null
	 * 
	 * @return
	 */
	@Nullable
	T defaultValue();
}
