package se.j4j.argumentparser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Describes values of the type <code>T</code>.
 * Useful when {@link Object#toString()} doesn't give you what you want.
 * 
 * @param <T> the type to describe
 */
public interface Describer<T>
{
	/**
	 * @param value the value to describe
	 * @return a {@link String} describing <code>value</code>
	 */
	@Nonnull
	String describe(@Nullable T value);
}
