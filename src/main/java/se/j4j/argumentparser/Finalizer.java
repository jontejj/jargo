package se.j4j.argumentparser;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;

import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

/**
 * <pre>
 * A mechanism for finalizing parsed values before they enter a {@link ParsedArguments} instance.
 * 
 * Pass the constructed {@link Finalizer} to {@link ArgumentBuilder#finalizeWith(Finalizer)}
 * 
 * @param <T> the type of value to finalize
 * </pre>
 */
public interface Finalizer<T>
{
	/**
	 * <b>Note:</b> this should not modify <code>value</code> in any way.
	 * 
	 * @param value the parsed value (not limited by any {@link Limiter} yet)
	 * @return the finalized value
	 */
	@CheckReturnValue
	@Nullable
	T finalizeValue(@Nullable T value);
}
