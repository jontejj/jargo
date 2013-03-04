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
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static se.softhouse.comeon.strings.Descriptions.format;
import static se.softhouse.comeon.strings.StringsUtil.NEWLINE;
import static se.softhouse.comeon.strings.StringsUtil.TAB;
import static se.softhouse.comeon.strings.StringsUtil.startsWithAndHasMore;
import static se.softhouse.jargo.Argument.ParameterArity.NO_ARGUMENTS;
import static se.softhouse.jargo.ArgumentBuilder.DEFAULT_SEPARATOR;
import static se.softhouse.jargo.ArgumentBuilder.ArgumentSettings.ArgumentPredicates.IS_OF_VARIABLE_ARITY;
import static se.softhouse.jargo.ArgumentBuilder.ArgumentSettings.ArgumentPredicates.IS_REQUIRED;
import static se.softhouse.jargo.ArgumentExceptions.forMissingArguments;
import static se.softhouse.jargo.ArgumentExceptions.forUnallowedRepetitionArgument;
import static se.softhouse.jargo.ArgumentExceptions.withMessage;
import static se.softhouse.jargo.ArgumentExceptions.wrapException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import se.softhouse.comeon.collections.CharacterTrie;
import se.softhouse.comeon.strings.StringsUtil;
import se.softhouse.jargo.ArgumentBuilder.ArgumentSettings;
import se.softhouse.jargo.ArgumentExceptions.UnexpectedArgumentException;
import se.softhouse.jargo.StringParsers.InternalStringParser;
import se.softhouse.jargo.internal.Texts.ProgrammaticErrors;
import se.softhouse.jargo.internal.Texts.UserErrors;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

@Immutable
final class CommandLineParserInstance
{
	/**
	 * A list where arguments created without names is put
	 */
	@Nonnull private final List<Argument<?>> indexedArguments;

	/**
	 * A map containing short-named arguments, long-named arguments and named arguments that ignore
	 * case
	 */
	@Nonnull private final NamedArguments namedArguments;

	/**
	 * Stores arguments that either has a special {@link ArgumentBuilder#separator()}, or is a
	 * {@link ArgumentBuilder#asPropertyMap()}. Handles {@link ArgumentBuilder#ignoreCase()} as
	 * well.
	 */
	@Nonnull private final SpecialArguments specialArguments;

	// TODO: this could be dynamically gathered as a view of indexedArguments, namedArguments and
	// specialArguments, worth it?
	@Nonnull private final Set<Argument<?>> allArguments;

	/**
	 * Used by {@link Command} to indicate that this parser is part of a {@link Command}
	 */
	private final boolean isCommandParser;

	private final ProgramInformation programInformation;

	CommandLineParserInstance(List<Argument<?>> argumentDefinitions, ProgramInformation information, boolean isCommandParser)
	{
		this.namedArguments = new NamedArguments(argumentDefinitions.size());
		this.specialArguments = new SpecialArguments();
		this.indexedArguments = newArrayListWithCapacity(argumentDefinitions.size());
		this.allArguments = newLinkedHashSetWithExpectedSize(argumentDefinitions.size());
		this.programInformation = information;
		this.isCommandParser = isCommandParser;
		for(Argument<?> definition : argumentDefinitions)
		{
			addArgumentDefinition(definition);
		}
		verifyThatIndexedAndRequiredArgumentsWasGivenBeforeAnyOptionalArguments();
		verifyUniqueMetasForRequiredAndIndexedArguments();
		verifyThatOnlyOneArgumentIsOfVariableArity();
	}

	CommandLineParserInstance(List<Argument<?>> argumentDefinitions, ProgramInformation information)
	{
		this(argumentDefinitions, information, false);
	}

	private void addArgumentDefinition(final Argument<?> definition)
	{
		if(definition.isIndexed())
		{
			indexedArguments.add(definition);
		}
		else
		{
			for(String name : definition.names())
			{
				addNamedArgumentDefinition(name, definition);
			}
		}
		boolean added = allArguments().add(definition);
		checkArgument(added, ProgrammaticErrors.UNIQUE_ARGUMENT, definition);
	}

	private void addNamedArgumentDefinition(final String name, final Argument<?> definition)
	{
		Argument<?> oldDefinition = null;
		String separator = definition.separator();
		if(definition.isPropertyMap())
		{
			oldDefinition = specialArguments.put(name, definition);
		}
		else if(separator.equals(DEFAULT_SEPARATOR))
		{
			oldDefinition = namedArguments.put(name, definition);
		}
		else
		{
			oldDefinition = specialArguments.put(name + separator, definition);
		}
		checkArgument(oldDefinition == null, ProgrammaticErrors.NAME_COLLISION, name);
	}

	private void verifyThatIndexedAndRequiredArgumentsWasGivenBeforeAnyOptionalArguments()
	{
		// Specifying the optional argument before the required argument would make the optional
		// argument required
		int lastRequiredIndexedArgument = 0;
		int firstOptionalIndexedArgument = Integer.MAX_VALUE;
		for(int i = 0; i < indexedArguments.size(); i++)
		{
			ArgumentSettings indexedArgument = indexedArguments.get(i);
			if(indexedArgument.isRequired())
			{
				lastRequiredIndexedArgument = i;
			}
			else if(firstOptionalIndexedArgument == Integer.MAX_VALUE)
			{
				firstOptionalIndexedArgument = i;
			}
		}
		checkArgument(	lastRequiredIndexedArgument <= firstOptionalIndexedArgument, ProgrammaticErrors.REQUIRED_ARGUMENTS_BEFORE_OPTIONAL,
						firstOptionalIndexedArgument, lastRequiredIndexedArgument);
	}

	private void verifyUniqueMetasForRequiredAndIndexedArguments()
	{
		// Otherwise the error texts becomes ambiguous
		Set<String> metasForRequiredAndIndexedArguments = newHashSetWithExpectedSize(indexedArguments.size());
		for(ArgumentSettings indexedArgument : filter(indexedArguments, IS_REQUIRED))
		{
			String meta = indexedArgument.metaDescriptionInRightColumn();
			boolean metaWasUnique = metasForRequiredAndIndexedArguments.add(meta);
			checkArgument(metaWasUnique, ProgrammaticErrors.UNIQUE_METAS, meta);
		}
	}

	private void verifyThatOnlyOneArgumentIsOfVariableArity()
	{
		// How would one know when the first argument considers itself satisfied?
		Collection<Argument<?>> indexedVariableArityArguments = filter(indexedArguments, IS_OF_VARIABLE_ARITY);
		checkArgument(indexedVariableArityArguments.size() <= 1, ProgrammaticErrors.SEVERAL_VARIABLE_ARITY_PARSERS, indexedVariableArityArguments);
	}

	@Nonnull
	ParsedArguments parse(final List<String> actualArguments, Locale localeToParseWith) throws ArgumentException
	{
		return parse(ArgumentIterator.forArguments(actualArguments), localeToParseWith);
	}

	@Nonnull
	ParsedArguments parse(ArgumentIterator arguments, Locale locale) throws ArgumentException
	{
		ParsedArguments holder = new ParsedArguments(allArguments());

		parseArguments(arguments, holder, locale);

		Collection<Argument<?>> missingArguments = holder.requiredArgumentsLeft();
		if(missingArguments.size() > 0)
			throw forMissingArguments(missingArguments).withUsage(usage(locale));

		for(Argument<?> arg : holder.parsedArguments())
		{
			arg.finalizeValue(holder);
			limitArgument(arg, holder, locale);
		}
		return holder;
	}

	private void parseArguments(final ArgumentIterator actualArguments, final ParsedArguments holder, Locale locale) throws ArgumentException
	{
		while(actualArguments.hasNext())
		{
			Argument<?> definition = null;
			try
			{
				definition = getDefinitionForCurrentArgument(actualArguments, holder, locale);
				if(definition == null)
				{
					break;
				}
				parseArgument(actualArguments, holder, definition, locale);
			}
			catch(ArgumentException e)
			{
				e.originatedFromArgumentName(actualArguments.getCurrentArgumentName());
				if(definition != null)
				{
					e.originatedFrom(definition);
				}
				throw e.withUsage(usage(locale));
			}
		}
	}

	private <T> void parseArgument(final ArgumentIterator arguments, final ParsedArguments parsedArguments, final Argument<T> definition,
			Locale locale) throws ArgumentException
	{
		T oldValue = parsedArguments.getValue(definition);

		if(oldValue != null && !definition.isAllowedToRepeat() && !definition.isPropertyMap())
			throw forUnallowedRepetitionArgument(arguments.current());

		InternalStringParser<T> parser = definition.parser();
		Locale localeToParseWith = definition.locale(locale);

		T parsedValue = parser.parse(arguments, oldValue, definition, localeToParseWith);
		parsedArguments.put(definition, parsedValue);
	}

	/**
	 * @return a definition that defines how to handle the current argument
	 * @throws UnexpectedArgumentException if no definition could be found
	 *             for the current argument
	 */
	@Nullable
	private Argument<?> getDefinitionForCurrentArgument(final ArgumentIterator arguments, final ParsedArguments holder, Locale locale)
			throws ArgumentException
	{
		// FIXME: what about already executed Commands, should this be validated in the outermost
		// parse?
		String currentArgument = checkNotNull(arguments.next(), "Argument strings may not be null");

		arguments.setCurrentArgumentName(currentArgument);

		Argument<?> byName = lookupByName(currentArgument, arguments);
		if(byName != null)
			return byName;

		Argument<?> option = batchOfShortNamedArguments(currentArgument, arguments, holder, locale);
		if(option != null)
			return option;

		Argument<?> indexedArgument = indexedArgument(arguments, holder);
		if(indexedArgument != null)
			return indexedArgument;

		if(isCommandParser())
		{
			// Rolling back here means that the parent parser/command will receive the argument
			// instead, maybe it can handle it
			// FIXME: this also means that Commands will have been executed if the command line was
			// invalid, how to prevent this? Maybe this is okay? Maybe provide a maven like "resume"
			// operation?
			arguments.previous();
			return null;
		}

		guessAndSuggestIfCloseMatch(currentArgument, holder);

		// We're out of order, tell the user what we didn't like
		throw ArgumentExceptions.forUnexpectedArgument(arguments);
	}

	private Argument<?> lookupByName(String currentArgument, ArgumentIterator arguments)
	{
		// Ordinary, named, arguments that directly matches the argument
		Argument<?> definition = namedArguments.get(currentArgument);
		if(definition != null)
			return definition;

		// Property Maps,Special separator, ignore case arguments
		Entry<String, Argument<?>> entry = specialArguments.get(currentArgument);
		if(entry != null)
		{
			// Remove "-D" from "-Dkey=value"
			String valueAfterSeparator = currentArgument.substring(entry.getKey().length());
			arguments.setNextArgumentTo(valueAfterSeparator);
			return entry.getValue();
		}
		return null;
	}

	/**
	 * Batch of short-named optional arguments
	 * For instance, "-fs" was used instead of "-f -s"
	 */
	private Argument<?> batchOfShortNamedArguments(String currentArgument, ArgumentIterator arguments, ParsedArguments holder, Locale locale)
			throws ArgumentException
	{
		if(startsWithAndHasMore(currentArgument, "-"))
		{
			List<Character> givenCharacters = Lists.charactersOf(currentArgument.substring(1));
			Set<Argument<?>> foundOptions = newLinkedHashSetWithExpectedSize(givenCharacters.size());
			Argument<?> lastOption = null;
			for(Character optionName : givenCharacters)
			{
				lastOption = namedArguments.get("-" + optionName);
				if(lastOption == null || lastOption.parameterArity() != NO_ARGUMENTS || !foundOptions.add(lastOption))
				{
					// Abort as soon as an unexpected character is discovered
					break;
				}
			}
			// Only accept the argument if all characters could be matched and no duplicate
			// characters were found
			if(foundOptions.size() == givenCharacters.size())
			{
				// The last option is handled with the return
				foundOptions.remove(lastOption);

				// A little ugly that this get method has side-effects but the alternative is to
				// return a list of arguments to parse and as this should be a rare case it's not
				// worth it, the result is the same at least
				for(Argument<?> option : foundOptions)
				{
					parseArgument(arguments, holder, option, locale);
				}
				return lastOption;
			}
		}
		return null;
	}

	private Argument<?> indexedArgument(ArgumentIterator arguments, ParsedArguments holder)
	{
		if(holder.indexedArgumentsParsed() < indexedArguments.size())
		{
			Argument<?> definition = indexedArguments.get(holder.indexedArgumentsParsed());
			arguments.previous();
			// This helps the error messages explain which of the indexed arguments that failed
			if(isCommandParser())
			{
				arguments.setCurrentArgumentName(arguments.usedCommandName());
			}
			else
			{
				arguments.setCurrentArgumentName(definition.metaDescriptionInRightColumn());
			}
			return definition;
		}
		return null;
	}

	/**
	 * Suggests probable, valid, alternatives for a faulty argument, based on the
	 * {@link StringsUtil#levenshteinDistance(String, String)}
	 */
	private void guessAndSuggestIfCloseMatch(String currentArgument, final ParsedArguments holder) throws ArgumentException
	{
		Set<String> availableArguments = nonParsedArguments(holder);

		if(!availableArguments.isEmpty())
		{
			List<String> suggestions = StringsUtil.closestMatches(currentArgument, availableArguments, ONLY_REALLY_CLOSE_MATCHES);
			// TODO: offer yes or no and continue the parsing on yes instead of throwing
			if(!suggestions.isEmpty())
				throw withMessage(format(UserErrors.SUGGESTION, currentArgument, NEW_LINE_AND_TAB.join(suggestions)));
		}
	}

	private Set<String> nonParsedArguments(final ParsedArguments holder)
	{
		Set<String> validArguments = Sets.newHashSetWithExpectedSize(allArguments().size());
		for(Argument<?> argument : allArguments())
		{
			if(!holder.wasGiven(argument) || argument.isAllowedToRepeat())
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
		}
		return validArguments;
	}

	private static final int ONLY_REALLY_CLOSE_MATCHES = 4;
	private static final Joiner NEW_LINE_AND_TAB = Joiner.on(NEWLINE + TAB);

	boolean isCommandParser()
	{
		return isCommandParser;
	}

	private <T> void limitArgument(@Nonnull Argument<T> arg, ParsedArguments holder, Locale locale) throws ArgumentException
	{
		try
		{
			arg.checkLimit(holder.getValue(arg));
		}
		catch(IllegalArgumentException e)
		{
			throw wrapException(e).originatedFrom(arg).originatedFromArgumentName(arg.toString()).withUsage(usage(locale));
		}
		catch(ArgumentException e)
		{
			throw e.originatedFrom(arg).withUsage(usage(locale));
		}
	}

	Set<Argument<?>> allArguments()
	{
		return allArguments;
	}

	@CheckReturnValue
	@Nonnull
	String commandUsage(Locale locale)
	{
		return new Usage(allArguments(), locale).forCommand();
	}

	Usage usage(Locale locale)
	{
		return new Usage(allArguments(), locale, programInformation);
	}

	/**
	 * Wraps a list of given arguments and remembers
	 * which argument that is currently being parsed.
	 */
	static final class ArgumentIterator extends UnmodifiableIterator<String>
	{
		private static final int NONE = -1;
		private final List<String> arguments;
		private int currentArgumentIndex;

		/**
		 * Corresponds to one of the {@link Argument#names()} that has been given from the command
		 * line.
		 * This is updated as soon as the parsing of a new argument begins.
		 * For indexed arguments this will be the meta description instead.
		 */
		private String currentArgumentName;

		private int indexOfLastCommand = NONE;

		/**
		 * @param actualArguments a list of arguments, will be modified
		 */
		private ArgumentIterator(List<String> actualArguments)
		{
			this.arguments = actualArguments;
		}

		void rememberAsCommand()
		{
			// The command has moved the index by 1 therefore the -1 to get the index of the
			// commandName
			indexOfLastCommand = currentArgumentIndex - 1;
		}

		/**
		 * For indexed arguments in commands the used command name is returned so that when
		 * multiple commands (or multiple command names) are used it's clear which command the
		 * offending argument is part of
		 */
		String usedCommandName()
		{
			return arguments.get(indexOfLastCommand);
		}

		static ArgumentIterator forArguments(List<String> actualArguments)
		{
			// specialSeparatorArguments, KeyValueParser etc may modify the list
			// so it should be a private copy
			return new ArgumentIterator(actualArguments);
		}

		/**
		 * Returns the string that was given by the previous {@link #next()} invocation.
		 */
		String current()
		{
			return arguments.get(currentArgumentIndex - 1);
		}

		@Override
		public boolean hasNext()
		{
			return currentArgumentIndex < arguments.size();
		}

		@Override
		public String next()
		{
			return arguments.get(currentArgumentIndex++);
		}

		String previous()
		{
			return arguments.get(--currentArgumentIndex);
		}

		int nrOfRemainingArguments()
		{
			return arguments.size() - currentArgumentIndex;
		}

		void setNextArgumentTo(String newNextArgumentString)
		{
			arguments.set(--currentArgumentIndex, newNextArgumentString);
		}

		boolean hasPrevious()
		{
			return currentArgumentIndex > 0;
		}

		void setCurrentArgumentName(String argumentName)
		{
			currentArgumentName = argumentName;
		}

		String getCurrentArgumentName()
		{
			return currentArgumentName;
		}

		@Override
		public String toString()
		{
			return arguments.toString();
		}
	}

	private static final class NamedArguments
	{
		private final Map<String, Argument<?>> namedArguments;

		NamedArguments(int expectedSize)
		{
			namedArguments = newHashMapWithExpectedSize(expectedSize);
		}

		Argument<?> put(String name, Argument<?> definition)
		{
			if(definition.isIgnoringCase())
				return namedArguments.put(name.toLowerCase(Locale.ENGLISH), definition);
			return namedArguments.put(name, definition);
		}

		Argument<?> get(String name)
		{
			Argument<?> definition = namedArguments.get(name);
			if(definition != null)
				return definition;

			String lowerCase = name.toLowerCase(Locale.ENGLISH);
			definition = namedArguments.get(lowerCase);
			if(definition != null && definition.isIgnoringCase())
				return definition;
			return null;
		}
	}

	private static final class SpecialArguments
	{
		private final CharacterTrie<Argument<?>> specialArguments;

		SpecialArguments()
		{
			specialArguments = CharacterTrie.newTrie();
		}

		Argument<?> put(String name, Argument<?> definition)
		{
			if(definition.isIgnoringCase())
				return specialArguments.put(name.toLowerCase(Locale.ENGLISH), definition);
			return specialArguments.put(name, definition);
		}

		Entry<String, Argument<?>> get(String name)
		{
			Entry<String, Argument<?>> entry = specialArguments.findLongestPrefix(name);
			if(entry != null)
				return entry;

			String lowerCase = name.toLowerCase(Locale.ENGLISH);
			entry = specialArguments.findLongestPrefix(lowerCase);
			if(entry != null && entry.getValue().isIgnoringCase())
				return entry;
			return null;
		}
	}

	static CommandLineParserInstance createCommandParser(List<Argument<?>> commandArguments)
	{
		if(commandArguments.isEmpty())
			return ParserCache.NO_ARG_COMMAND_PARSER;
		return new CommandLineParserInstance(commandArguments, ProgramInformation.AUTO, true);
	}

	/**
	 * Cache for commonly constructed parsers
	 */
	@VisibleForTesting
	static final class ParserCache
	{
		private ParserCache()
		{
		}

		static final CommandLineParserInstance NO_ARG_COMMAND_PARSER = new CommandLineParserInstance(Collections.<Argument<?>>emptyList(),
				ProgramInformation.AUTO,
				true);
	}
}
