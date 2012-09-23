package se.j4j.strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Describes values of the type {@code T}.
 * Useful when {@link Object#toString()} doesn't give you what you want.
 * 
 * @param <T> the type to describe
 */
@Immutable
public interface Describer<T>
{
	/**
	 * @param value the value to describe
	 * @return a {@link String} describing {@code value}
	 */
	@Nonnull
	String describe(@Nullable T value);
}
