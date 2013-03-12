/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.softhouse.jargo;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static se.softhouse.jargo.Argument.ArgumentPredicates.IS_REQUIRED;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.softhouse.jargo.internal.Texts.ProgrammaticErrors;

import com.google.common.collect.Sets;

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
	// FIXME Map<Argument<?>, Optional<Object>>? to differentiate nulls from absence
	@Nonnull private final Map<Argument<?>, Object> parsedArguments = newLinkedHashMap();
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

	/**
	 * Checks if an {@link Argument} was given in the command line arguments
	 * 
	 * @param argument the {@link Argument} to check
	 * @return true if {@code argument} was given, that is, the default value will not be used
	 */
	public boolean wasGiven(Argument<?> argument)
	{
		return parsedArguments.containsKey(checkNotNull(argument));
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

	Set<Argument<?>> parsedArguments()
	{
		return parsedArguments.keySet();
	}

	Set<String> nonParsedArguments()
	{
		Set<String> validArguments = Sets.newHashSetWithExpectedSize(allArguments.size());
		for(Argument<?> argument : allArguments)
		{
			boolean wasGiven = wasGiven(argument);
			if(!wasGiven || argument.isAllowedToRepeat())
			{
				for(String name : argument.names())
				{
					if(argument.separator().equals(ArgumentBuilder.DEFAULT_SEPARATOR))
					{
						validArguments.add(name);
					}
					else
					{
						validArguments.add(name + argument.separator());
					}

				}
			}
			if(wasGiven && argument.parser() instanceof Command)
			{
				// TODO: test suggestions for sub commands as well
				ParsedArguments lastInvocation = (ParsedArguments) getValue(argument);
				validArguments.addAll(lastInvocation.nonParsedArguments());
			}
		}
		return validArguments;
	}
}
