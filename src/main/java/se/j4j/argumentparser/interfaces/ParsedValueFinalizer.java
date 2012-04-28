package se.j4j.argumentparser.interfaces;

import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

/**
 * A callback mechanism for finalizing parsed values before they
 * enter a {@link ParsedArguments} instance (which is {@link Immutable}.
 * Pass the constructed {@link ParsedValueFinalizer} to
 * {@link ArgumentBuilder#parsedValueFinalizer(ParsedValueFinalizer)}
 * 
 * @param <T> the type of value that has been parsed
 */
public interface ParsedValueFinalizer<T>
{
	/**
	 * <b>Note:</b> this method is not called for default values as they are
	 * considered finalized as they are
	 * 
	 * @param value the parsed value (not validated yet)
	 */
	T finalizeValue(T value);
}
