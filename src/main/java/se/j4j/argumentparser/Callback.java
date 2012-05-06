package se.j4j.argumentparser;

import javax.annotation.Nullable;

/**
 * Called when values have been parsed. {@link #parsedValue(Object)} is called
 * after all {@link Finalizer#finalizeValue(Object)} and
 * {@link Limiter#withinLimits(Object)} methods have been called.
 * 
 * @param <T>
 */
public interface Callback<T>
{
	void parsedValue(@Nullable T parsedValue);
}
