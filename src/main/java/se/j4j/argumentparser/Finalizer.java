package se.j4j.argumentparser;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

/**
 * A mechanism for finalizing parsed values before they
 * enter a {@link ParsedArguments} instance (which is {@link Immutable}.
 * Pass the constructed {@link Finalizer} to
 * {@link ArgumentBuilder#finalizeWith(Finalizer)}
 * 
 * @param <T> the type of value that has been parsed and should be finalized
 */
public interface Finalizer<T>
{
	/**
	 * <b>Note:</b> this method is not called for default values as they are
	 * considered finalized as they are
	 * 
	 * @param value the parsed value (not limited yet)
	 * @return the finalized value
	 */
	@CheckReturnValue
	T finalizeValue(T value);
}
