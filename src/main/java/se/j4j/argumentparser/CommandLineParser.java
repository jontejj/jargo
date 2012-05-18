package se.j4j.argumentparser;

import java.util.List;
import java.util.ListIterator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;

/**
 * Parses {@link Argument}s. See {@link CommandLineParsers} for implementations.
 * The implementations of this interface are the brains that make all classes in this package work
 * together.
 */
@Immutable
public interface CommandLineParser
{
	@Nonnull
	ParsedArguments parse(@Nonnull final String ... actualArguments) throws ArgumentException;

	@Nonnull
	ParsedArguments parse(@Nonnull final List<String> actualArguments) throws ArgumentException;

	@Nonnull
	ParsedArguments parse(@Nonnull ListIterator<String> actualArguments) throws ArgumentException;

	/**
	 * Returns a String describing this {@link CommandLineParser}.
	 * Suitable to print on {@link System#out}.
	 */
	@CheckReturnValue
	@Nonnull
	String usage(@Nonnull final String programName);
}
