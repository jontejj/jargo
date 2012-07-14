package se.j4j.argumentparser;

import javax.annotation.Nullable;

/**
 * <pre>
 * Called when values have been parsed.
 * 
 * Integrated into an {@link Argument} with {@link ArgumentBuilder#callbackForValues(Callback)}.
 * </pre>
 * 
 * @param <T>
 */
public interface Callback<T>
{
	/**
	 * Tells the callback that a value has been parsed.
	 * This is called after {@link Finalizer#finalizeValue(Object)} and
	 * {@link Limiter#withinLimits(Object)} have been called.
	 * 
	 * @param parsedValue the parsed value
	 */
	void parsedValue(@Nullable T parsedValue);
}
