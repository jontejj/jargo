package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Maps.newIdentityHashMap;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static java.lang.Math.max;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_NAMED;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_OF_VARIABLE_ARITY;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_REQUIRED;
import static se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings.IS_VISIBLE;
import static se.j4j.argumentparser.ArgumentExceptions.forMissingArguments;
import static se.j4j.argumentparser.ArgumentExceptions.forUnallowedRepetitionArgument;
import static se.j4j.argumentparser.ArgumentExceptions.forUnexpectedArgument;
import static se.j4j.argumentparser.ArgumentExceptions.wrapException;
import static se.j4j.argumentparser.ArgumentFactory.command;
import static se.j4j.strings.StringsUtil.NEWLINE;
import static se.j4j.strings.StringsUtil.spaces;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.StringParsers.OptionParser;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UsageTexts;
import se.j4j.collections.CharacterTrie;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Manages multiple {@link Argument}s and/or {@link Command}s.
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * import static se.j4j.argumentparser.ArgumentFactory.*;
 * ...
 * String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};
 * 
 * Argument&lt;Boolean&gt; enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();
 * Argument&lt;Integer&gt; port = integerArgument("-p", "--listen-port").defaultValue(8080).description("The port clients should connect to.").build();
 * Argument&lt;String&gt; greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();
 * 
 * try
 * {
 *   ParsedArguments parsedValues = CommandLineParser.withArguments(greetingPhrase, enableLogging, port).parse(args);
 *   assertThat(parsedValues.get(enableLogging)).isTrue();
 *   assertThat(parsedValues.get(port)).isEqualTo(8090);
 *   assertThat(parsedValues.get(greetingPhrase)).isEqualTo("Hello");
 * }
 * catch(ArgumentException exception)
 * {
 *   System.out.println(exception.getMessageAndUsage("YourProgramName"));
 *   System.exit(1);
 * }
 * </code>
 * </pre>
 * 
 * <pre>
 * If something goes wrong during the parsing (Missing required arguments, Unexpected arguments, Invalid values),
 * it will be described by the ArgumentException. Use {@link ArgumentException#getMessageAndUsage(String)} if you
 * want to explain what went wrong to the user.
 * </pre>
 */
@Immutable
public final class CommandLineParser
{
	/**
	 * Creates a CommandLineParser with support for the given {@code argumentDefinitions}.
	 * 
	 * @param argumentDefinitions {@link Argument}s produced with {@link ArgumentFactory} or
	 *            with your own disciples of {@link ArgumentBuilder}
	 * @return a CommandLineParser which you can call {@link CommandLineParser#parse(String...)} on
	 *         and get {@link ParsedArguments} out of.
	 * @throws IllegalArgumentException if the given {@code argumentDefinitions} would create an
	 *             erroneous CommandLineParser
	 * @see CommandLineParser
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser withArguments(final Argument<?> ... argumentDefinitions)
	{
		return new CommandLineParser(Arrays.asList(argumentDefinitions));
	}

	/**
	 * {@link Iterable} version of {@link #withArguments(Argument...)}
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser withArguments(final Iterable<Argument<?>> argumentDefinitions)
	{
		return new CommandLineParser(copyOf(argumentDefinitions));
	}

	/**
	 * Creates a {@link CommandLineParser} supporting the given {@code commands}.
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser withCommands(final Command ... commands)
	{
		List<Argument<?>> arguments = newArrayListWithCapacity(commands.length);
		for(Command c : commands)
		{
			arguments.add(command(c).build());
		}
		return new CommandLineParser(arguments);
	}

	/**
	 * Parses {@code actualArguments} (typically from the command line, i.e argv) and returns the
	 * parsed values in a {@link ParsedArguments} container.
	 * 
	 * @throws ArgumentException if an invalid argument is encountered during the parsing
	 */
	@Nonnull
	public ParsedArguments parse(final String ... actualArguments) throws ArgumentException
	{
		return parse(newArrayList(actualArguments));
	}

	/**
	 * {@link Iterator} version of {@link #parse(String...)}
	 */
	@Nonnull
	public ParsedArguments parse(final Iterator<String> actualArguments) throws ArgumentException
	{
		return parse(newArrayList(actualArguments));
	}

	/**
	 * {@link Iterable} version of {@link #parse(String...)}
	 */
	@Nonnull
	public ParsedArguments parse(final Iterable<String> actualArguments) throws ArgumentException
	{
		return parse(newArrayList(actualArguments));
	}

	/**
	 * Returns a usage text describing all arguments this {@link CommandLineParser} handles.
	 * Suitable to print on {@link System#out}.
	 */
	@CheckReturnValue
	@Nonnull
	public String usage(final String programName)
	{
		// TODO: maybe print one line with all options
		return new Usage().withProgramName(programName);
	}

	@Override
	public String toString()
	{
		return usage("CommandLineParser#toString");
	}

	/**
	 * Holds parsed arguments for a {@link CommandLineParser#parse(String...)} invocation.
	 * Use {@link #get(Argument)} to fetch a parsed command line value.
	 */
	@Immutable
	public static final class ParsedArguments
	{
		@Nonnull private final ParsedArgumentHolder holder;

		private ParsedArguments(final ParsedArgumentHolder holder)
		{
			this.holder = holder;
		}

		/**
		 * @param argumentToFetch
		 * @return the parsed value for the given {@code argumentToFetch},
		 *         if no value was given on the command line the {@link Argument#defaultValue()} is
		 *         returned.
		 */
		@Nullable
		@CheckReturnValue
		public <T> T get(final Argument<T> argumentToFetch)
		{
			T value = holder.getValue(argumentToFetch);
			if(value == null)
				return argumentToFetch.defaultValue();

			return value;
		}

		@Override
		public String toString()
		{
			return holder.toString();
		}

		@Override
		public int hashCode()
		{
			return holder.parsedArguments.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(!(obj instanceof ParsedArguments))
				return false;

			ParsedArguments other = (ParsedArguments) obj;
			return holder.isEqualTo(other.holder);
		}
	}

	// End of public API

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

	@Nonnull private final Set<Argument<?>> allArguments;

	/**
	 * Used by {@link Command} to indicate that this parser is part of a {@link Command}
	 */
	private final boolean isCommandParser;

	CommandLineParser(List<Argument<?>> argumentDefinitions, boolean isCommandParser)
	{
		namedArguments = new NamedArguments(argumentDefinitions.size());
		specialArguments = new SpecialArguments();
		indexedArguments = newArrayListWithCapacity(argumentDefinitions.size());
		allArguments = newLinkedHashSetWithExpectedSize(argumentDefinitions.size());
		this.isCommandParser = isCommandParser;
		for(Argument<?> definition : argumentDefinitions)
		{
			addArgumentDefinition(definition);
		}
		verifyThatIndexedAndRequiredArgumentsWasGivenBeforeAnyOptionalArguments();
		verifyUniqueMetasForRequiredAndIndexedArguments();

		// How would one know when the first argument considers itself satisfied?
		Collection<Argument<?>> indexedVariableArityArguments = filter(indexedArguments, IS_OF_VARIABLE_ARITY);
		checkArgument(indexedVariableArityArguments.size() <= 1, ProgrammaticErrors.SEVERAL_VARIABLE_ARITY_PARSERS, indexedVariableArityArguments);
	}

	CommandLineParser(List<Argument<?>> argumentDefinitions)
	{
		this(argumentDefinitions, false);
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
		boolean added = allArguments.add(definition);
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
		else if(separator != null)
		{
			oldDefinition = specialArguments.put(name + separator, definition);
		}
		else
		{
			oldDefinition = namedArguments.put(name, definition);
		}
		checkArgument(oldDefinition == null, ProgrammaticErrors.NAME_COLLISION, name);
	}

	private void verifyThatIndexedAndRequiredArgumentsWasGivenBeforeAnyOptionalArguments()
	{
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

	@Nonnull
	private ParsedArguments parse(final List<String> actualArguments) throws ArgumentException
	{
		return parse(ArgumentIterator.forArguments(actualArguments));
	}

	@Nonnull
	ParsedArguments parse(ArgumentIterator arguments) throws ArgumentException
	{
		ParsedArgumentHolder holder = new ParsedArgumentHolder(allArguments);

		parseArguments(arguments, holder);

		Collection<Argument<?>> missingArguments = holder.requiredArgumentsLeft();
		if(missingArguments.size() > 0)
			throw forMissingArguments(missingArguments, this);

		for(Argument<?> arg : holder.parsedArguments.keySet())
		{
			arg.finalizeValue(holder);
			limitArgument(arg, holder);
		}
		return new ParsedArguments(holder);
	}

	private void parseArguments(final ArgumentIterator actualArguments, final ParsedArgumentHolder holder) throws ArgumentException
	{
		actualArguments.markStartOfParse();
		while(actualArguments.hasNext())
		{
			Argument<?> definition = null;
			try
			{
				definition = getDefinitionForCurrentArgument(actualArguments, holder);
				if(definition == null)
				{
					break;
				}
				parseArgument(actualArguments, holder, definition);
			}
			catch(ArgumentException e)
			{
				e.originatedFromArgumentName(actualArguments.getCurrentArgumentName());
				e.originatedFrom(definition);
				throw e.originatedFrom(this);
			}
		}
		actualArguments.markEndOfParse();
	}

	private <T> void parseArgument(final ArgumentIterator arguments, final ParsedArgumentHolder parsedArguments, final Argument<T> definition)
			throws ArgumentException
	{
		T oldValue = parsedArguments.getValue(definition);

		// TODO: maybe null was the result of a previous argument
		if(oldValue != null && !definition.isAllowedToRepeat() && !definition.isPropertyMap())
			throw forUnallowedRepetitionArgument(arguments.current());

		InternalStringParser<T> parser = definition.parser();

		T parsedValue = parser.parse(arguments, oldValue, definition);
		parsedArguments.put(definition, parsedValue);
	}

	/**
	 * @return a definition that defines how to handle an argument
	 * @throws UnexpectedArgumentException if no definition could be found
	 *             for the current argument
	 */
	@Nullable
	private Argument<?> getDefinitionForCurrentArgument(final ArgumentIterator arguments, final ParsedArgumentHolder holder) throws ArgumentException
	{
		String currentArgument = checkNotNull(arguments.next(), "Argument strings may not be null");

		arguments.setCurrentArgumentName(currentArgument);

		// Ordinary, named, arguments that directly matches the argument
		Argument<?> definition = namedArguments.get(currentArgument);
		if(definition != null)
			return definition;

		// Property Maps,Special separator, ignore case arguments
		Map.Entry<CharSequence, Argument<?>> entry = specialArguments.get(currentArgument);
		if(entry != null)
		{
			// Remove "--name=" from "--name=value"
			String valueAfterSeparator = currentArgument.substring(entry.getKey().length());
			arguments.setNextArgumentTo(valueAfterSeparator);
			return entry.getValue();
		}

		// Batch of short-named optional arguments
		// For instance, "-fs" was used instead of "-f -s"
		if(currentArgument.startsWith("-") && currentArgument.length() > 1)
		{
			List<Character> givenCharacters = Lists.charactersOf(currentArgument.substring(1));
			Set<Argument<?>> foundOptions = newLinkedHashSetWithExpectedSize(givenCharacters.size());
			Argument<?> lastOption = null;
			for(Character optionName : givenCharacters)
			{
				lastOption = namedArguments.get("-" + optionName);
				if(lastOption == null || lastOption.parameterArity() != OptionParser.NO_ARGUMENTS || !foundOptions.add(lastOption))
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
				// worth it
				for(Argument<?> option : foundOptions)
				{
					parseArgument(arguments, holder, option);
				}
				return lastOption;
			}
		}

		// Indexed arguments
		if(holder.indexedArgumentsParsed < indexedArguments.size())
		{
			definition = indexedArguments.get(holder.indexedArgumentsParsed);
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

		if(isCommandParser())
		{
			// Rolling back here means that the parent parser/command will receive the argument
			// instead, maybe it can handle it
			arguments.previous();
			return null;
		}

		// TODO: suggest alternative options/parameters based on the
		// faulty characters' distance (keyboard wise (consider dvorak))
		// Ask Did you mean and provide y/n (Also give the option to remember this misspelling)

		// We're out of order, tell the user what we didn't like
		throw forUnexpectedArgument(arguments);
	}

	private boolean isCommandParser()
	{
		return isCommandParser;
	}

	private <T> void limitArgument(@Nonnull Argument<T> arg, ParsedArgumentHolder holder) throws ArgumentException
	{
		try
		{
			arg.checkLimit(holder.getValue(arg));
		}
		catch(IllegalArgumentException e)
		{
			throw wrapException(e).originatedFrom(this).originatedFromArgumentName(arg.toString()).originatedFrom(arg);
		}
		catch(ArgumentException e)
		{
			throw e.originatedFrom(this).originatedFrom(arg);
		}
	}

	// TODO: move out these private classes into an internal package
	private final class Usage
	{
		private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 40;
		private static final int SPACES_BETWEEN_COLUMNS = 4;

		private final Joiner NAME_JOINER = Joiner.on(UsageTexts.NAME_SEPARATOR);

		private final Iterable<Argument<?>> argumentsToPrint = Iterables.filter(sortedArguments(), IS_VISIBLE);

		/**
		 * The builder to append usage texts to
		 */
		@Nonnull private final StringBuilder builder;

		/**
		 * <pre>
		 * For:
		 * -l, --enable-logging Output debug information to standard out
		 * -p, --listen-port    The port clients should connect to.
		 * 
		 * This would be 20.
		 */
		private final int indexOfDescriptionColumn;
		private boolean needsNewline = false;

		Usage()
		{
			indexOfDescriptionColumn = determineLongestNameColumn() + SPACES_BETWEEN_COLUMNS;
			builder = new StringBuilder(expectedUsageTextSize());
		}

		private Iterable<Argument<?>> sortedArguments()
		{
			Iterable<Argument<?>> indexedWithoutVariableArity = filter(indexedArguments, not(IS_OF_VARIABLE_ARITY));
			Iterable<Argument<?>> indexedWithVariableArity = filter(indexedArguments, IS_OF_VARIABLE_ARITY);
			List<Argument<?>> sortedArgumentsByName = newArrayList(filter(allArguments, IS_NAMED));
			// TODO: sort in a lexicographical way?
			Collections.sort(sortedArgumentsByName);
			return Iterables.concat(indexedWithoutVariableArity, sortedArgumentsByName, indexedWithVariableArity);
		}

		private int determineLongestNameColumn()
		{
			int longestNameSoFar = 0;
			for(Argument<?> arg : argumentsToPrint)
			{
				longestNameSoFar = max(longestNameSoFar, lengthOfNameColumn(arg));
			}
			return longestNameSoFar;
		}

		private int lengthOfNameColumn(final Argument<?> argument)
		{
			int namesLength = 0;

			for(String name : argument.names())
			{
				namesLength += name.length();
			}
			int separatorLength = max(0, UsageTexts.NAME_SEPARATOR.length() * (argument.names().size() - 1));

			int metaLength = argument.metaDescriptionInLeftColumn().length();

			return namesLength + separatorLength + metaLength;
		}

		private int expectedUsageTextSize()
		{
			// Two lines for each argument
			return 2 * allArguments.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION);
		}

		@CheckReturnValue
		@Nonnull
		String withProgramName(final String programName)
		{
			mainUsage(programName);
			for(Argument<?> arg : argumentsToPrint)
			{
				usageForArgument(arg);
			}
			return toString();
		}

		private void mainUsage(final String programName)
		{
			if(!isCommandParser())
			{
				builder.append(UsageTexts.USAGE_HEADER + programName);
			}
			if(!isEmpty(argumentsToPrint))
			{
				builder.append(UsageTexts.OPTIONS);
				builder.append(NEWLINE);
			}
		}

		@Override
		public String toString()
		{
			return builder.toString();
		};

		/**
		 * <pre>
		 * 	-foo   Foo something [Required]
		 *         	Valid values: 1 to 5
		 *        -bar   Bar something
		 *         	Default: 0
		 * </pre>
		 */
		@Nonnull
		private void usageForArgument(final Argument<?> arg)
		{
			int lengthBeforeCurrentArgument = builder.length();

			NAME_JOINER.appendTo(builder, arg.names());

			builder.append(arg.metaDescriptionInLeftColumn());

			int lengthOfFirstColumn = builder.length() - lengthBeforeCurrentArgument;
			builder.append(spaces(indexOfDescriptionColumn - lengthOfFirstColumn));

			// TODO: handle long descriptions, names, meta descriptions, default value
			// descriptions
			String description = arg.description();
			if(!description.isEmpty())
			{
				builder.append(description);
				addIndicators(arg);
				needsNewline = true;
				newlineWithIndentation();
				valueExplanation(arg);
			}
			else
			{
				valueExplanation(arg);
				addIndicators(arg);
			}
			builder.append(NEWLINE);
			needsNewline = false;
		}

		private void newlineWithIndentation()
		{
			if(needsNewline)
			{
				builder.append(NEWLINE);
				builder.append(spaces(indexOfDescriptionColumn));
				needsNewline = false;
			}
		}

		private <T> void addIndicators(final Argument<T> arg)
		{
			if(arg.isRequired())
			{
				builder.append(UsageTexts.REQUIRED);
			}
			if(arg.isAllowedToRepeat())
			{
				builder.append(UsageTexts.ALLOWS_REPETITIONS);
			}
		}

		private <T> void valueExplanation(final Argument<T> arg)
		{
			// TODO: handle long value explanations, replace each newline with enough spaces,
			// split up long lines
			String description = arg.descriptionOfValidValues();
			if(!description.isEmpty())
			{
				boolean isCommand = arg.parser() instanceof Command;
				if(isCommand)
				{
					// For commands the validValues is a usage text itself for the command arguments
					// +1 = indentation so that command options are tucked under the command
					String spaces = spaces(indexOfDescriptionColumn + 1);
					description = description.replace(NEWLINE, NEWLINE + spaces);
				}
				else
				{
					String meta = arg.metaDescriptionInRightColumn();
					builder.append(meta + ": ");
				}

				builder.append(description);
				needsNewline = true;
			}
			if(arg.isRequired())
				return;

			String descriptionOfDefaultValue = arg.defaultValueDescription();
			if(descriptionOfDefaultValue != null)
			{
				newlineWithIndentation();
				String spaces = spaces(indexOfDescriptionColumn + UsageTexts.DEFAULT_VALUE_START.length());
				descriptionOfDefaultValue = descriptionOfDefaultValue.replace(NEWLINE, NEWLINE + spaces);

				builder.append(UsageTexts.DEFAULT_VALUE_START).append(descriptionOfDefaultValue);
			}
		}
	}

	/**
	 * Holds the currently parsed values.
	 * This is a static class so that it doesn't keep unnecessary references to the
	 * {@link CommandLineParser} that created it.
	 */
	@NotThreadSafe
	static final class ParsedArgumentHolder
	{
		/**
		 * Stores results from {@link StringParser#parse(String)}
		 */
		@Nonnull private final Map<Argument<?>, Object> parsedArguments = newIdentityHashMap();
		@Nonnull private final Set<Argument<?>> allArguments;
		/**
		 * Keeps a running total of how many indexed arguments that have been parsed
		 */
		private int indexedArgumentsParsed = 0;

		ParsedArgumentHolder(Set<Argument<?>> arguments)
		{
			allArguments = arguments;
		}

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
			if(value == null)
			{
				checkArgument(allArguments.contains(definition), ProgrammaticErrors.ILLEGAL_ARGUMENT, definition);
			}
			return value;
		}

		@Override
		public String toString()
		{
			return parsedArguments.toString();
		}

		boolean isEqualTo(ParsedArgumentHolder other)
		{
			return parsedArguments.equals(other.parsedArguments);
		}

		Collection<Argument<?>> requiredArgumentsLeft()
		{
			return filter(allArguments, and(not(in(parsedArguments.keySet())), IS_REQUIRED));
		}
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
			arguments = actualArguments;
		}

		void markStartOfParse()
		{
			// The command has moved the index by 1 therefore the -1 to get the index of the
			// commandName
			indexOfLastCommand = currentArgumentIndex - 1;
		}

		void markEndOfParse()
		{
			indexOfLastCommand = NONE;
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

		private static ArgumentIterator forArguments(List<String> actualArguments)
		{
			// specialSeparatorArguments, KeyValueParser etc may modify the list
			// so it should be a private copy
			return new ArgumentIterator(actualArguments);
		}

		static ArgumentIterator forSingleArgument(String argument)
		{
			return new ArgumentIterator(Arrays.asList(argument));
		}

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
			// TODO: verify the usage of Locale.ENGLISH
			if(definition.isIgnoringCase())
				return specialArguments.put(name.toLowerCase(Locale.ENGLISH), definition);
			return specialArguments.put(name, definition);
		}

		Map.Entry<CharSequence, Argument<?>> get(String name)
		{
			Entry<CharSequence, Argument<?>> entry = specialArguments.getLastMatchingEntry(name);
			if(entry != null)
				return entry;

			String lowerCase = name.toLowerCase(Locale.ENGLISH);
			entry = specialArguments.getLastMatchingEntry(lowerCase);
			if(entry != null && entry.getValue().isIgnoringCase())
				return entry;
			return null;
		}
	}

	static CommandLineParser createCommandParser(List<Argument<?>> commandArguments)
	{
		if(commandArguments.isEmpty())
			return ParserCache.NO_ARG_COMMAND_PARSER;
		return new CommandLineParser(commandArguments, true);
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

		static final CommandLineParser NO_ARG_COMMAND_PARSER = new CommandLineParser(Collections.<Argument<?>>emptyList(), true);
	}
}
