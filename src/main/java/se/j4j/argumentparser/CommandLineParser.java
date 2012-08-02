package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Maps.newIdentityHashMap;
import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static java.lang.Math.max;
import static se.j4j.argumentparser.ArgumentExceptions.forMissingArguments;
import static se.j4j.argumentparser.ArgumentExceptions.forUnallowedRepetitionArgument;
import static se.j4j.argumentparser.ArgumentExceptions.forUnexpectedArgument;
import static se.j4j.argumentparser.ArgumentFactory.command;
import static se.j4j.argumentparser.internal.Platform.NEWLINE;
import static se.j4j.argumentparser.internal.StringsUtil.spaces;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.ArgumentExceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.StringParsers.OptionParser;
import se.j4j.argumentparser.StringParsers.VariableArityParser;
import se.j4j.argumentparser.internal.CharacterTrie;
import se.j4j.argumentparser.internal.Texts;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Parses {@link Argument}s.
 * Starting point for the call chain:
 * 
 * <pre>
 * {@code
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
 * 	ParsedArguments arguments = CommandLineParser.forArguments(greetingPhrase, enableLogging, port).parse(args);
 * 	assertTrue(enableLogging + " was not found in parsed arguments", arguments.get(enableLogging));
 * }
 * catch(ArgumentException exception)
 * {
 * 	System.out.println(exception.getMessageAndUsage("YourProgramName"));
 * 	System.exit(1);
 * }
 * 
 * }
 * 
 * If something goes wrong during the parsing (Missing required arguments, Unexpected arguments, Invalid values),
 * it will be described by the ArgumentException. Use {@link ArgumentException#getMessageAndUsage(String)} if you
 * want to explain what went wrong to the user.
 * 
 * @param argumentDefinitions {@link Argument}s produced with {@link ArgumentFactory} or
 * with your own disciples of {@link ArgumentBuilder}
 * @return a CommandLineParser which you can call {@link CommandLineParser#parse(String...)} on
 * and get {@link ParsedArguments} out of.
 * @throws IllegalArgumentException if two or more of the given arguments
 *             uses the same name (either short or long name)
 * </pre>
 */
@Immutable
public final class CommandLineParser
{
	/**
	 * Creates a CommandLineParser with support for the given {@code argumentDefinitions}.
	 * 
	 * @see CommandLineParser
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser forArguments(@Nonnull final Argument<?> ... argumentDefinitions)
	{
		return new CommandLineParser(Arrays.asList(argumentDefinitions));
	}

	/**
	 * @see #forArguments(Argument...)
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser forArguments(@Nonnull final Iterable<Argument<?>> argumentDefinitions)
	{
		return new CommandLineParser(copyOf(argumentDefinitions));
	}

	/**
	 * Creates a {@link CommandLineParser} supporting the given {@code commands}.
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser forCommands(@Nonnull final Command ... commands)
	{
		List<Argument<?>> arguments = newArrayListWithCapacity(commands.length);
		for(Command c : commands)
		{
			arguments.add(command(c).build());
		}
		return new CommandLineParser(arguments);
	}

	@Nonnull
	public ParsedArguments parse(@Nonnull final Iterator<String> actualArguments) throws ArgumentException
	{
		return parse(newArrayList(actualArguments));
	}

	@Nonnull
	public ParsedArguments parse(@Nonnull final Iterable<String> actualArguments) throws ArgumentException
	{
		return parse(newArrayList(actualArguments));
	}

	@Nonnull
	public ParsedArguments parse(@Nonnull final String ... actualArguments) throws ArgumentException
	{
		return parse(newArrayList(actualArguments));
	}

	/**
	 * Returns a usage text describing this {@link CommandLineParser}.
	 * Suitable to print on {@link System#out}.
	 */
	@CheckReturnValue
	@Nonnull
	public String usage(@Nonnull final String programName)
	{
		return new Usage().withProgramName(programName);
	}

	CommandLineParser(@Nonnull List<Argument<?>> argumentDefinitions, boolean isCommandParser)
	{
		namedArguments = new NamedArguments(argumentDefinitions.size());
		indexedArguments = newArrayListWithCapacity(argumentDefinitions.size());
		specialSeparatorArguments = CharacterTrie.newTrie();
		ignoreCaseSpecialSeparatorArguments = CharacterTrie.newTrie();
		requiredArguments = newLinkedHashSetWithExpectedSize(argumentDefinitions.size());
		propertyMapArguments = CharacterTrie.newTrie();
		ignoreCasePropertyMapArguments = CharacterTrie.newTrie();
		allArguments = newHashSetWithExpectedSize(argumentDefinitions.size());
		this.isCommandParser = isCommandParser;
		for(Argument<?> definition : argumentDefinitions)
		{
			addArgumentDefinition(definition);
		}
		verifyThatIndexedAndRequiredArgumentsWasGivenBeforeAnyOptionalArguments();

		// How would one know when the first argument considers itself satisfied?
		Collection<Argument<?>> unnamedVariableArityArguments = filter(indexedArguments, IS_OF_VARIABLE_ARITY);
		checkArgument(unnamedVariableArityArguments.size() <= 1, Texts.SEVERAL_VARIABLE_ARITY_PARSERS, unnamedVariableArityArguments);
	}

	CommandLineParser(@Nonnull List<Argument<?>> argumentDefinitions)
	{
		this(argumentDefinitions, false);
	}

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
	 * A map with arguments that has special {@link Argument#separator()}s
	 */
	@Nonnull private final CharacterTrie<Argument<?>> specialSeparatorArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s
	 * and {@link Argument#isIgnoringCase()}
	 */
	@Nonnull private final CharacterTrie<Argument<?>> ignoreCaseSpecialSeparatorArguments;

	@Nonnull private final CharacterTrie<Argument<?>> propertyMapArguments;

	@Nonnull private final CharacterTrie<Argument<?>> ignoreCasePropertyMapArguments;

	/**
	 * If arguments are required, set by calling {@link Argument#required()} ,
	 * and they aren't given on the command line, {@link MissingRequiredArgumentException} is
	 * thrown when {@link #parse(String...)} is called.
	 */
	@Nonnull private final Set<Argument<?>> requiredArguments;

	@Nonnull private final Set<Argument<?>> allArguments;

	/**
	 * Used by {@link Command} to indicate that this parser is part of a {@link Command}
	 */
	private final boolean isCommandParser;

	private void addArgumentDefinition(@Nonnull final Argument<?> definition)
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
		if(definition.isRequired())
		{
			requiredArguments.add(definition);
		}
		boolean added = allArguments.add(definition);
		checkArgument(added, Texts.UNIQUE_ARGUMENT, definition);
	}

	private void addNamedArgumentDefinition(@Nonnull final String key, @Nonnull final Argument<?> definition)
	{
		Argument<?> oldDefinition = null;
		String separator = definition.separator();
		if(definition.isPropertyMap())
		{
			oldDefinition = propertyMapArguments.set(key, definition);
			if(definition.isIgnoringCase())
			{
				// TODO: verify the usage of Locale.ENGLISH
				ignoreCasePropertyMapArguments.set(key.toLowerCase(Locale.ENGLISH), definition);
			}
		}
		else if(separator != null)
		{
			oldDefinition = specialSeparatorArguments.set(key + separator, definition);
			if(definition.isIgnoringCase())
			{
				ignoreCaseSpecialSeparatorArguments.set((key + separator).toLowerCase(Locale.ENGLISH), definition);
			}
		}
		else
		{
			oldDefinition = namedArguments.put(key, definition);
		}
		checkArgument(oldDefinition == null, Texts.NAME_COLLISION, key);
	}

	private void verifyThatIndexedAndRequiredArgumentsWasGivenBeforeAnyOptionalArguments()
	{
		// Also verify that required & indexed arguments have unique meta descriptions, otherwise
		// the error texts becomes ambiguous
		Set<String> metasForRequiredAndIndexedArguments = newHashSetWithExpectedSize(indexedArguments.size());

		int indexOfLastRequiredIndexedArgument = 0;
		int indexOfFirstOptionalIndexedArgument = Integer.MAX_VALUE;
		for(int i = 0; i < indexedArguments.size(); i++)
		{
			ArgumentSettings indexedArgument = indexedArguments.get(i);
			if(indexedArgument.isRequired())
			{
				indexOfLastRequiredIndexedArgument = i;
				String meta = indexedArgument.metaDescriptionInRightColumn();
				boolean metaWasUnique = metasForRequiredAndIndexedArguments.add(meta);
				checkArgument(metaWasUnique, Texts.UNIQUE_METAS, meta);
			}
			else
			{
				indexOfFirstOptionalIndexedArgument = i;
			}
		}
		checkArgument(	indexOfLastRequiredIndexedArgument <= indexOfFirstOptionalIndexedArgument, Texts.REQUIRED_ARGUMENTS_BEFORE_OPTIONAL,
						indexOfFirstOptionalIndexedArgument, indexOfLastRequiredIndexedArgument);
	}

	@Nonnull
	private ParsedArguments parse(@Nonnull final List<String> actualArguments) throws ArgumentException
	{
		return parse(ArgumentIterator.forArguments(actualArguments));
	}

	@Nonnull
	ParsedArguments parse(@Nonnull ArgumentIterator arguments) throws ArgumentException
	{
		ParsedArgumentHolder holder = new ParsedArgumentHolder(requiredArguments);

		parseArguments(arguments, holder);

		if(!holder.requiredArgumentsLeft.isEmpty())
			throw forMissingArguments(holder.requiredArgumentsLeft, this);

		for(Argument<?> arg : holder.parsedArguments.keySet())
		{
			arg.finalizeValue(holder);
			limitArgument(arg, holder);
		}
		return new ParsedArguments(holder);
	}

	private void parseArguments(@Nonnull final ArgumentIterator actualArguments, @Nonnull final ParsedArgumentHolder holder) throws ArgumentException
	{
		actualArguments.markStartOfParse();
		while(actualArguments.hasNext())
		{
			try
			{
				Argument<?> definition = getDefinitionForCurrentArgument(actualArguments, holder);
				if(definition == null)
				{
					break;
				}
				parseArgument(actualArguments, holder, definition);
			}
			catch(ArgumentException e)
			{
				e.originatedFromArgumentName(actualArguments.getCurrentArgumentName());
				throw e.originatedFrom(this);
			}
		}
		actualArguments.markEndOfParse();
	}

	private <T> void parseArgument(@Nonnull final ArgumentIterator arguments, @Nonnull final ParsedArgumentHolder parsedArguments,
			@Nonnull final Argument<T> definition) throws ArgumentException
	{
		T oldValue = parsedArguments.getValue(definition);

		// TODO: maybe null was the result of a previous argument
		if(oldValue != null && !definition.isAllowedToRepeat() && !definition.isPropertyMap())
			throw forUnallowedRepetitionArgument(arguments.current());

		InternalStringParser<T> parser = definition.parser();

		T parsedValue = parser.parse(arguments, oldValue, definition);

		// TODO: parsedValue could be put into Argument in a ThreadLocal and be available by a get()
		// method
		parsedArguments.put(definition, parsedValue);
	}

	/**
	 * @return a definition that defines how to handle an argument
	 * @throws UnexpectedArgumentException if no definition could be found
	 *             for the current argument
	 */
	@Nullable
	private Argument<?> getDefinitionForCurrentArgument(@Nonnull final ArgumentIterator arguments, @Nonnull final ParsedArgumentHolder holder)
			throws ArgumentException
	{
		String currentArgument = arguments.next();
		arguments.setCurrentArgumentName(currentArgument);

		// Ordinary, named, arguments
		Argument<?> definition = namedArguments.get(currentArgument);
		if(definition != null)
			return definition;

		String lowerCase = currentArgument.toLowerCase(Locale.ENGLISH);

		// Property Maps
		definition = propertyMapArguments.getLastMatch(currentArgument);
		if(definition == null)
		{
			definition = ignoreCasePropertyMapArguments.getLastMatch(lowerCase);
		}
		if(definition != null)
		{
			arguments.previous();
			return definition;
		}

		// Special separator arguments
		CharSequence matchingKey;
		definition = specialSeparatorArguments.getLastMatch(currentArgument);
		if(definition != null)
		{
			matchingKey = specialSeparatorArguments.getMatchingKey(currentArgument);
		}
		else
		{
			definition = ignoreCaseSpecialSeparatorArguments.getLastMatch(lowerCase);
			matchingKey = ignoreCaseSpecialSeparatorArguments.getMatchingKey(lowerCase);
		}

		if(definition != null)
		{
			// Remove "--name=" from "--name=value"
			String valueAfterSeparator = currentArgument.substring(matchingKey.length());
			arguments.setNextArgumentTo(valueAfterSeparator);
			return definition;
		}

		// Batch of short-named optional arguments
		// For instance, "-fs" was used instead of "-f -s"
		if(currentArgument.startsWith("-") && currentArgument.length() > 1)
		{
			List<Character> optionCharacters = Lists.charactersOf(currentArgument.substring(1));
			Set<Argument<?>> foundOptions = newLinkedHashSetWithExpectedSize(optionCharacters.size());
			Argument<?> lastOption = null;
			for(Character optionName : optionCharacters)
			{
				lastOption = namedArguments.get("-" + optionName);
				if(lastOption == null || !(lastOption.parser() instanceof OptionParser) || !foundOptions.add(lastOption))
				{
					// Abort as soon we discover an unexpected character
					break;
				}
			}
			// Only accept the argument if all characters could be matched and no duplicate
			// characters were found
			if(foundOptions.size() == optionCharacters.size())
			{
				// The last option is handled with the return
				foundOptions.remove(lastOption);

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
			// To give better error explanations we use the meta description for indexed arguments
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
			// Rolling back here means that the next parser/command will receive the argument
			// instead because maybe it can handle it
			arguments.previous();
			return null;
		}

		// TODO: suggest alternative options/parameters based on the
		// faulty characters' distance (keyboard wise (consider dvorak))
		// Ask Did you mean and provide y/n

		// We're out of order, tell the user what we didn't like
		throw forUnexpectedArgument(arguments);
	}

	private boolean isCommandParser()
	{
		return isCommandParser;
	}

	@Override
	public String toString()
	{
		return usage("CommandLineParser#toString");
	}

	private <T> void limitArgument(@Nonnull Argument<T> arg, @Nonnull ParsedArgumentHolder holder) throws ArgumentException
	{
		try
		{
			arg.checkLimit(holder.getValue(arg));
		}
		catch(ArgumentException e)
		{
			e.originatedFrom(this);
			throw e;
		}
	}

	private final class Usage
	{
		/**
		 * The characters that, for arguments with several names, separates the different names
		 */
		private static final String NAME_SEPARATOR = ", ";
		private static final String DEFAULT_VALUE_START = "Default: ";
		private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 40;
		private static final int SPACES_BETWEEN_COLUMNS = 4;

		private final Joiner NAME_JOINER = Joiner.on(NAME_SEPARATOR);

		private final List<Argument<?>> argumentsToPrint = newArrayListWithCapacity(allArguments.size());

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
			for(Argument<?> arg : sortedArguments())
				if(!arg.isHiddenFromUsage())
				{
					argumentsToPrint.add(arg);
				}

			indexOfDescriptionColumn = determineLongestNameColumn() + SPACES_BETWEEN_COLUMNS;
			builder = new StringBuilder(expectedUsageTextSize());
		}

		private Iterable<Argument<?>> sortedArguments()
		{
			Iterable<Argument<?>> indexedWithoutVariableArity = filter(indexedArguments, not(IS_OF_VARIABLE_ARITY));
			Iterable<Argument<?>> indexedWithVariableArity = filter(indexedArguments, IS_OF_VARIABLE_ARITY);
			List<Argument<?>> sortedArgumentsByName = newArrayList(filter(allArguments, IS_NAMED));
			// TODO: place commands last?
			Collections.sort(sortedArgumentsByName, BY_FIRST_NAME);

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

		private int lengthOfNameColumn(@Nonnull final Argument<?> argument)
		{
			int namesLength = 0;

			for(String name : argument.names())
			{
				namesLength += name.length();
			}
			int separatorLength = max(0, NAME_SEPARATOR.length() * (argument.names().size() - 1));

			int metaLength = argument.metaDescriptionInLeftColumn().length();

			return namesLength + separatorLength + metaLength;
		}

		private int expectedUsageTextSize()
		{
			// Two lines for each argument
			return 2 * argumentsToPrint.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION);
		}

		@CheckReturnValue
		@Nonnull
		String withProgramName(@Nonnull final String programName)
		{
			mainUsage(programName);
			for(Argument<?> arg : argumentsToPrint)
			{
				usageForArgument(arg);
			}
			return toString();
		}

		private void mainUsage(@Nonnull final String programName)
		{
			if(!isCommandParser())
			{
				builder.append(Texts.USAGE_HEADER + programName);
			}
			if(!argumentsToPrint.isEmpty())
			{
				builder.append(Texts.OPTIONS);
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
		private void usageForArgument(@Nonnull final Argument<?> arg)
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

		private <T> void addIndicators(@Nonnull final Argument<T> arg)
		{
			if(arg.isRequired())
			{
				builder.append(Texts.REQUIRED);
			}
			if(arg.isAllowedToRepeat())
			{
				builder.append(Texts.ALLOWS_REPETITIONS);
			}
			// TODO: mention ignoreCase?
		}

		private <T> void valueExplanation(@Nonnull final Argument<T> arg)
		{
			// TODO: handle long value explanations, replace each newline with enough spaces,
			// split up long lines
			String description = arg.descriptionOfValidValues();
			if(!description.isEmpty())
			{
				boolean isCommand = arg.parser() instanceof Command;
				if(!isCommand)
				{
					String meta = arg.metaDescriptionInRightColumn();
					builder.append(meta + ": ");
				}
				else
				{
					// +1 = indentation so that command options are tucked under the command
					String spaces = spaces(indexOfDescriptionColumn + 1);
					description = description.replace(NEWLINE, NEWLINE + spaces);
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
				String spaces = spaces(indexOfDescriptionColumn + DEFAULT_VALUE_START.length());
				descriptionOfDefaultValue = descriptionOfDefaultValue.replace(NEWLINE, NEWLINE + spaces);

				builder.append(DEFAULT_VALUE_START).append(descriptionOfDefaultValue);
			}
		}
	}

	/**
	 * Holds parsed arguments for a {@link CommandLineParser#parse(String...)} invocation.
	 * Use {@link #get(Argument)} to fetch a parsed command line value.
	 */
	@Immutable
	public static final class ParsedArguments
	{
		@Nonnull private final ParsedArgumentHolder holder;

		private ParsedArguments(@Nonnull final ParsedArgumentHolder holder)
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
		public <T> T get(@Nonnull final Argument<T> argumentToFetch)
		{
			T value = holder.getValue(argumentToFetch);
			if(value == null)
			{
				// TODO: if the argument wasn't given to the CommandLineParser it should'nt be able
				// to modify here
				T defaultValue = argumentToFetch.defaultValue();
				holder.put(argumentToFetch, defaultValue);
				// TODO: make this thread safe, this should always be run for arguments that isn't
				// given
				return defaultValue;
			}

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
				return false; // Final class so instance of is proper

			ParsedArguments other = (ParsedArguments) obj;
			return holder.isEqualTo(other.holder);
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
		@Nonnull final Map<Argument<?>, Object> parsedArguments = newIdentityHashMap();
		@Nonnull final Set<Argument<?>> requiredArgumentsLeft;
		/**
		 * Keeps a running total of how many indexed arguments that have been parsed
		 */
		int indexedArgumentsParsed = 0;

		ParsedArgumentHolder(Set<Argument<?>> requiredArguments)
		{
			requiredArgumentsLeft = newLinkedHashSet(requiredArguments);
		}

		public <T> void put(@Nonnull final Argument<T> definition, @Nullable final T value)
		{
			if(definition.isRequired())
			{
				requiredArgumentsLeft.remove(definition);
			}
			if(definition.isIndexed() && !parsedArguments.containsKey(definition))
			{
				indexedArgumentsParsed++;
			}
			parsedArguments.put(definition, value);
		}

		public <T> T getValue(@Nonnull final Argument<T> definition)
		{
			// Safe because put guarantees that the map is heterogeneous
			@SuppressWarnings("unchecked")
			T value = (T) parsedArguments.get(definition);
			return value;
		}

		@Override
		public String toString()
		{
			return parsedArguments.toString();
		}

		public boolean isEqualTo(ParsedArgumentHolder other)
		{
			return parsedArguments.equals(other.parsedArguments);
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

		String usedCommandName()
		{
			// For indexed arguments in commands we return the used command name so that when
			// multiple commands (or multiple command names) are used it's clear which command the
			// offending argument is part of
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

		public NamedArguments(int expectedSize)
		{
			namedArguments = newHashMapWithExpectedSize(expectedSize);
		}

		public Argument<?> put(String name, Argument<?> definition)
		{
			if(definition.isIgnoringCase())
				return namedArguments.put(name.toLowerCase(Locale.ENGLISH), definition);
			return namedArguments.put(name, definition);
		}

		public Argument<?> get(String name)
		{
			Argument<?> definition = namedArguments.get(name);
			if(definition != null)
				return definition;

			String lowerCase = name.toLowerCase(Locale.ENGLISH);
			definition = namedArguments.get(lowerCase);
			return definition;
		}
	}

	// Predicates

	private static final Predicate<ArgumentSettings> IS_NAMED = new Predicate<ArgumentSettings>(){
		@Override
		public boolean apply(ArgumentSettings input)
		{
			return !input.isIndexed();
		}
	};

	private static final Predicate<Argument<?>> IS_OF_VARIABLE_ARITY = new Predicate<Argument<?>>(){
		@Override
		public boolean apply(Argument<?> input)
		{
			return (input.parser() instanceof VariableArityParser);
		}
	};

	// Comparators

	private static final Comparator<ArgumentSettings> BY_FIRST_NAME = new Comparator<ArgumentSettings>(){
		@Override
		public int compare(@Nonnull final ArgumentSettings one, @Nonnull final ArgumentSettings two)
		{
			String name = one.names().get(0);
			return name.compareToIgnoreCase(two.names().get(0));
		}
	};
}
