package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Maps.newIdentityHashMap;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.ArgumentPredicates.IS_REQUIRED;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;

/**
 * Holds parsed arguments for a {@link CommandLineParser#parse(String...)} invocation.
 * Use {@link #get(Argument)} to fetch a parsed command line value.
 */
@Immutable
public final class ParsedArguments
{
	/**
	 * Stores results from {@link StringParser#parse(String, Locale)}
	 */
	// TODO Map<Argument<?>, Optional<Object>>? to differentiate nulls from absence
	@Nonnull private final Map<Argument<?>, Object> parsedArguments = newIdentityHashMap();
	@Nonnull private final Set<Argument<?>> allArguments;
	/**
	 * Keeps a running total of how many indexed arguments that have been parsed
	 */
	private int indexedArgumentsParsed = 0;

	ParsedArguments(Set<Argument<?>> arguments)
	{
		allArguments = arguments;
	}

	/**
	 * Returns the parsed value for the given {@code argumentToFetch},
	 * if no value was given on the command line the default value is
	 * returned.
	 * 
	 * @see StringParser#defaultValue()
	 */
	@Nullable
	@CheckReturnValue
	public <T> T get(final Argument<T> argumentToFetch)
	{
		checkNotNull(argumentToFetch);

		T value = getValue(argumentToFetch);

		if(value == null)
		{
			checkArgument(allArguments.contains(argumentToFetch), ProgrammaticErrors.ILLEGAL_ARGUMENT, argumentToFetch);
			return argumentToFetch.defaultValue();
		}

		return value;
	}

	@Override
	public String toString()
	{
		return parsedArguments.toString();
	}

	@Override
	public boolean equals(@Nullable Object other)
	{
		if(this == other)
			return true;
		if(!(other instanceof ParsedArguments))
			return false;

		return parsedArguments.equals(((ParsedArguments) other).parsedArguments);
	}

	@Override
	public int hashCode()
	{
		return parsedArguments.hashCode();
	}

	// Publicly this class is Immutable, CommandLineParserInstance is only allowed to modify it
	// during parsing

	<T> void put(final Argument<T> definition, @Nullable final T value)
	{
		if(definition.isIndexed())
		{
			indexedArgumentsParsed++;
		}
		parsedArguments.put(definition, value);
	}

	<T> T getValue(final Argument<T> definition)
	{
		// Safe because put guarantees that the map is heterogeneous
		@SuppressWarnings("unchecked")
		T value = (T) parsedArguments.get(definition);
		return value;
	}

	Collection<Argument<?>> requiredArgumentsLeft()
	{
		return filter(allArguments, and(not(in(parsedArguments.keySet())), IS_REQUIRED));
	}

	int indexedArgumentsParsed()
	{
		return indexedArgumentsParsed;
	}

	boolean hasParsed(ArgumentSettings argument)
	{
		return parsedArguments.containsKey(argument);
	}

	Set<Argument<?>> parsedArguments()
	{
		return parsedArguments.keySet();
	}
}
