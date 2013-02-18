package se.j4j.strings;

import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Describes values of the type {@code T} (or subclasses of {@code T}). Useful
 * when {@link Object#toString()} doesn't give you what you want.
 * 
 * @param <T> the type to describe
 * @see Describers
 */
@Immutable
public interface Describer<T>
{
	/**
	 * @param value the value to describe
	 * @param inLocale the {@link Locale} to use when formatting the resulting {@link String}
	 * @return a {@link String} describing {@code value}
	 */
	@CheckReturnValue
	@Nonnull
	String describe(T value, Locale inLocale);
}
