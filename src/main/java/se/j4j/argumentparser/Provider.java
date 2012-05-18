package se.j4j.argumentparser;

import javax.annotation.Nullable;

/**
 * <pre>
 * Understands how to only create values (lazily) when they're needed.
 * 
 * If you only want {@link #provideValue()} to be called once wrap your provider with
 * {@link Providers#cachingProvider(Provider)}.
 * </pre>
 * 
 * @param <T> the type to provide
 */
public interface Provider<T>
{
	@Nullable
	T provideValue();
}
