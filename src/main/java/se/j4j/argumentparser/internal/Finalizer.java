package se.j4j.argumentparser.internal;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.ForwardingStringParser.SimpleForwardingStringParser;
import se.j4j.argumentparser.StringParser;

/**
 * <pre>
 * A mechanism for finalizing parsed values before they enter a {@link ParsedArguments} instance.
 * 
 * It's used by internal string parsers that supports repeated values, those need to wrap
 * lists, maps etc. in unmodifiable ones before the parsing is said to be complete.
 * 
 * For regular {@link StringParser}s it's recommended to use {@link SimpleForwardingStringParser}
 * and decorate your {@link StringParser} with any finalization there instead.
 * 
 * @param <T> the type of value to finalize
 * </pre>
 */
@Immutable
public interface Finalizer<T>
{
	/**
	 * <b>Note:</b> this should not modify {@code value} in any way.
	 * 
	 * @param value the parsed value
	 * @return the finalized value
	 */
	@CheckReturnValue
	@Nullable
	T finalizeValue(@Nullable T value);
}
