package se.j4j.argumentparser;

import static com.google.common.base.Predicates.in;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newIdentityHashMap;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.google.common.collect.Sets.newLinkedHashSetWithExpectedSize;
import static java.lang.Math.max;
import static se.j4j.argumentparser.ArgumentExceptions.forMissingArguments;
import static se.j4j.argumentparser.ArgumentExceptions.forUnexpectedArgument;
import static se.j4j.argumentparser.ArgumentExceptions.forUnhandledRepeatedArgument;
import static se.j4j.argumentparser.ArgumentFactory.command;
import static se.j4j.argumentparser.internal.ListUtil.copy;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentExceptions.LimitException;
import se.j4j.argumentparser.ArgumentExceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.ArgumentExceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.argumentparser.internal.Lines;
import se.j4j.argumentparser.internal.StringsUtil;
import se.j4j.argumentparser.internal.TrieTree;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Gives you static access to implementations of the {@link CommandLineParser} interface.
 */
public final class CommandLineParsers
{
	private CommandLineParsers()
	{
	}

	/**
	 * Starting point for the call chain:
	 * 
	 * <pre>
	 * <code>
	 * import static se.j4j.argumentparser.ArgumentFactory.*;
	 * ...
	 * String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};
	 * 
	 * Argument&lt;Boolean&gt; enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out");
	 * Argument&lt;Integer&gt; port = integerArgument("-p", "--listen-port").defaultValue(8080).description("The port clients should connect to.");
	 * Argument&lt;String&gt; greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with");
	 * 
	 * try
	 * {
	 * 	ParsedArguments arguments = CommandLineParsers.forArguments(greetingPhrase, enableLogging, port).parse(args);
	 * 	assertTrue(enableLogging + " was not found in parsed arguments", arguments.get(enableLogging));
	 * }
	 * catch(ArgumentException exception)
	 * {
	 * 	System.out.println(exception.getMessageAndUsage("YourProgramName"));
	 * 	System.exit(1);
	 * }
	 * 
	 * </code>
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
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser forArguments(@Nonnull final Argument<?> ... argumentDefinitions)
	{
		return new DefaultArgumentParser(Arrays.asList(argumentDefinitions));
	}

	/**
	 * See {@link #forArguments(Argument...)}
	 * 
	 * @param argumentDefinitions the {@link List} of {@link Argument}s to support
	 * @return
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser forArguments(@Nonnull final List<Argument<?>> argumentDefinitions)
	{
		return new DefaultArgumentParser(argumentDefinitions);
	}

	/**
	 * Creates a {@link CommandLineParser} supporting the given <code>commands</code>
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser forCommands(@Nonnull final Command ... commands)
	{
		List<Argument<?>> arguments = Lists.newArrayListWithExpectedSize(commands.length);
		for(Command c : commands)
		{
			arguments.add(command(c).build());
		}
		return new DefaultArgumentParser(arguments);
	}

	/**
	 * @return a {@link CommandLineParser} that ignores any arguments given.
	 */
	public static CommandLineParser forAnyArguments()
	{
		return IgnoringCommandLineParser.INSTANCE;
	}

	private static final class DefaultArgumentParser extends CommandLineParserAdapter
	{
		private DefaultArgumentParser(@Nonnull final List<Argument<?>> argumentDefinitions)
		{
			namedArguments = Maps.newHashMapWithExpectedSize(argumentDefinitions.size());
			indexedArguments = Lists.newArrayListWithExpectedSize(argumentDefinitions.size());
			specialSeparatorArguments = TrieTree.newTree();
			ignoreCaseArguments = Maps.newHashMap();
			ignoreCaseSpecialSeparatorArguments = TrieTree.newTree();
			requiredArguments = newLinkedHashSetWithExpectedSize(argumentDefinitions.size());
			propertyMapArguments = TrieTree.newTree();
			ignoreCasePropertyMapArguments = TrieTree.newTree();
			allArguments = Sets.newHashSetWithExpectedSize(argumentDefinitions.size());
			for(Argument<?> definition : argumentDefinitions)
			{
				addArgumentDefinition(definition);
			}
		}

		/**
		 * A list where arguments created without names is put
		 */
		@Nonnull private final List<Argument<?>> indexedArguments;

		/**
		 * A map containing both short-named and long-named arguments
		 */
		@Nonnull private final Map<String, Argument<?>> namedArguments;

		/**
		 * A map with arguments that has special {@link Argument#separator()}s
		 */
		@Nonnull private final TrieTree<Argument<?>> specialSeparatorArguments;

		/**
		 * Map for arguments that's {@link Argument#isIgnoringCase()}.
		 * Stores it's keys with lower case.
		 */
		@Nonnull private final Map<String, Argument<?>> ignoreCaseArguments;

		/**
		 * A map with arguments that has special {@link Argument#separator()}s
		 * and {@link Argument#isIgnoringCase()}
		 */
		@Nonnull private final TrieTree<Argument<?>> ignoreCaseSpecialSeparatorArguments;

		@Nonnull private final TrieTree<Argument<?>> propertyMapArguments;

		@Nonnull private final TrieTree<Argument<?>> ignoreCasePropertyMapArguments;

		/**
		 * If arguments are required, set by calling {@link Argument#required()} ,
		 * and they aren't given on the command line, {@link MissingRequiredArgumentException} is
		 * thrown when {@link #parse(String...)} is called.
		 * TODO: should parse handle this printout itself instead of throwing?
		 */
		@Nonnull private final Set<Argument<?>> requiredArguments;

		@Nonnull private final Set<Argument<?>> allArguments;

		private void addArgumentDefinition(@Nonnull final Argument<?> definition)
		{
			if(definition.isNamed())
			{
				for(String name : definition.names())
				{
					addNamedArgumentDefinition(name, definition);
				}
			}
			else
			{
				indexedArguments.add(definition);
			}
			if(definition.isRequired())
			{
				requiredArguments.add(definition);
			}
			boolean added = allArguments.add(definition);
			if(!added)
				throw new IllegalArgumentException(definition + " handles the same argument twice");
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
					ignoreCasePropertyMapArguments.set(key.toLowerCase(Locale.getDefault()), definition);
				}
			}
			else if(separator != null)
			{
				oldDefinition = specialSeparatorArguments.set(key + separator, definition);
				if(definition.isIgnoringCase())
				{
					ignoreCaseSpecialSeparatorArguments.set((key + separator).toLowerCase(Locale.getDefault()), definition);
				}
			}
			else
			{
				oldDefinition = namedArguments.put(key, definition);
				if(definition.isIgnoringCase())
				{
					// TODO: as ignoreCase may match some arguments in
					// namedArguments that it shouldn't, we need to loop over every argument
					// and check for duplicates
					ignoreCaseArguments.put(key.toLowerCase(Locale.getDefault()), definition);
				}
			}
			if(oldDefinition != null)
				throw new IllegalArgumentException(definition + " handles the same argument as: " + oldDefinition);
		}

		@Override
		@Nonnull
		public ParsedArguments parse(@Nonnull ListIterator<String> actualArguments) throws ArgumentException
		{
			// specialSeparatorArguments, KeyValueParser etc may modify the list
			ListIterator<String> arguments = copy(actualArguments);

			ParsedArgumentHolder holder = new ParsedArgumentHolder(requiredArguments);
			while(arguments.hasNext())
			{
				parseArgument(arguments, holder);
			}

			if(!holder.requiredArgumentsLeft.isEmpty())
				throw forMissingArguments(holder.requiredArgumentsLeft, this);

			for(Argument<?> arg : holder.parsedArguments.keySet())
			{
				arg.finalizeValue(holder);
				limitArgument(arg, holder);
			}

			for(Argument<?> arg : holder.parsedArguments.keySet())
			{
				arg.parsedValue(holder);
			}
			return new ParsedArguments(holder);
		}

		private void parseArgument(@Nonnull final ListIterator<String> actualArguments, @Nonnull final ParsedArgumentHolder holder)
				throws ArgumentException

		{
			Argument<?> definition = null;
			try
			{
				definition = getDefinitionForCurrentArgument(actualArguments, holder);
				parseArgument(actualArguments, holder, definition);
			}
			catch(ArgumentException e)
			{
				e.setOriginParser(this);
				e.errorneousArgument(definition);
				throw e;
			}
		}

		private <T> void parseArgument(@Nonnull final ListIterator<String> actualArguments, @Nonnull final ParsedArgumentHolder parsedArguments,
				@Nonnull final Argument<T> definition) throws ArgumentException
		{
			T oldValue = parsedArguments.getValue(definition);

			// TODO: maybe null was the result of a previous argument
			if(oldValue != null && !definition.isAllowedToRepeat() && !definition.isPropertyMap())
				throw forUnhandledRepeatedArgument(definition, oldValue);

			InternalStringParser<T> parser = definition.parser();

			T parsedValue = parser.parse(actualArguments, oldValue, definition);

			parsedArguments.put(definition, parsedValue);
		}

		/**
		 * @param actualArguments
		 * @param holder
		 * @return a definition that defines how to handle an argument
		 * @throws UnexpectedArgumentException if no definition could be found
		 *             for the current argument
		 */
		@Nonnull
		private Argument<?> getDefinitionForCurrentArgument(@Nonnull final ListIterator<String> actualArguments,
				@Nonnull final ParsedArgumentHolder holder) throws UnexpectedArgumentException
		{
			String currentArgument = actualArguments.next();
			Argument<?> definition = namedArguments.get(currentArgument);

			if(definition != null)
				return definition;
			String lowerCase = currentArgument.toLowerCase(Locale.getDefault());

			definition = ignoreCaseArguments.get(lowerCase);
			if(definition != null)
				return definition;
			definition = propertyMapArguments.getLastMatch(currentArgument);
			if(definition == null)
			{
				definition = ignoreCasePropertyMapArguments.getLastMatch(lowerCase);
			}
			if(definition != null)
			{
				actualArguments.previous();
				return definition;
			}
			definition = ignoreCaseSpecialSeparatorArguments.getLastMatch(lowerCase);
			if(definition == null)
			{
				definition = specialSeparatorArguments.getLastMatch(currentArgument);
			}

			if(definition != null)
			{
				// Remove "name=" from "name=value"
				actualArguments.set(currentArgument.substring(1 + currentArgument.indexOf(definition.separator())));
				actualArguments.previous();
			}
			else
			{
				if(holder.unnamedArgumentsParsed >= indexedArguments.size())
					/**
					 * TODO: handle "-fs" as well as "-f -s"
					 */
					// TODO: suggest alternative options/parameters based on the
					// faulty characters' distance (keyboard wise (consider
					// dvorak))
					// Ask Did you mean and provide y/n
					throw forUnexpectedArgument(actualArguments);

				definition = indexedArguments.get(holder.unnamedArgumentsParsed);
				actualArguments.previous();
			}
			return definition;
		}

		@Override
		public String toString()
		{
			return new Usage().withProgramName("<main class>");
		}

		/**
		 * Returns a String describing this CommandLineParser.
		 * Suitable to print on {@link System#out}.
		 */
		@Override
		public String usage(@Nonnull final String programName)
		{
			return new Usage().withProgramName(programName);
		}

		private <T> void limitArgument(@Nonnull Argument<T> arg, @Nonnull ParsedArgumentHolder holder) throws LimitException
		{
			arg.checkLimit(holder.getValue(arg));
		}

		private final class Usage
		{
			@CheckReturnValue
			@Nonnull
			String withProgramName(@Nonnull final String programName)
			{
				mainUsage(programName);

				List<Argument<?>> sortedArgumentsByName = newArrayList(filter(allArguments, not(in(indexedArguments))));
				Collections.sort(sortedArgumentsByName, new NamedArgumentsByFirstName());

				for(Argument<?> arg : Iterables.concat(indexedArguments, sortedArgumentsByName))
				{
					usageForArgument(arg);
				}
				return toString();
			}

			@Override
			public String toString()
			{
				return builder.toString();
			};

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

			private static final int CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION = 30;
			private static final int SPACES_BETWEEN_COLUMNS = 4;

			private int expectedUsageTextSize()
			{
				// Two lines for each argument
				return 2 * allArguments.size() * (indexOfDescriptionColumn + CHARACTERS_IN_AVERAGE_ARGUMENT_DESCRIPTION);
			}

			Usage()
			{
				indexOfDescriptionColumn = determineLongestNameColumn() + SPACES_BETWEEN_COLUMNS;
				builder = new StringBuilder(expectedUsageTextSize());
			}

			private void mainUsage(@Nonnull final String programName)
			{
				builder.append("Usage: " + programName);
				if(!allArguments.isEmpty())
				{
					builder.append(" [Options]");
					builder.append(Lines.NEWLINE);
				}

			}

			private int determineLongestNameColumn()
			{
				int longestNames = 0;
				for(Argument<?> arg : allArguments)
				{
					int length = lengthOfFirstColumn(arg);
					if(length > longestNames)
					{
						longestNames = length;
					}
				}
				return longestNames;
			}

			private int lengthOfFirstColumn(@Nonnull final Argument<?> argument)
			{
				if(argument.shouldBeHiddenInUsage())
					return 0;

				int namesLength = 0;

				for(String name : argument.names())
				{
					namesLength += name.length();
				}
				int separatorLength = max(0, NAME_SEPARATOR.length() * (argument.names().size() - 1));

				int metaLength = argument.metaDescription().length();

				return namesLength + separatorLength + metaLength;
			}

			/**
			 * <pre>
			 * 	-test   Test something [Required]
			 *         	Valid values: 1 to 5
			 *        -test   Test something
			 *         	Default: 0
			 * </pre>
			 */
			@Nonnull
			private String usageForArgument(@Nonnull final Argument<?> arg)
			{
				if(arg.shouldBeHiddenInUsage())
					return "";

				int lengthOfFirstColumn = lengthOfFirstColumn(arg);

				Joiner.on(NAME_SEPARATOR).appendTo(builder, arg.names());

				builder.append(arg.metaDescription());

				StringsUtil.appendSpaces(indexOfDescriptionColumn - lengthOfFirstColumn, builder);
				// TODO: handle long descriptions
				// TODO: handle arity
				if(!arg.description().isEmpty())
				{
					builder.append(arg.description());
					addIndicators(arg);
					builder.append(Lines.NEWLINE);
					StringsUtil.appendSpaces(indexOfDescriptionColumn, builder);
				}
				else
				{
					addIndicators(arg);
				}
				builder.append(valueExplanation(arg));
				builder.append(Lines.NEWLINE);
				return builder.toString();
			}

			private <T> void addIndicators(@Nonnull final Argument<T> arg)
			{
				if(arg.isRequired())
				{
					builder.append(" [Required]");
				}
				if(arg.isAllowedToRepeat())
				{
					builder.append(" [Supports Multiple occurences]");
				}
			}

			private <T> StringBuilder valueExplanation(@Nonnull final Argument<T> arg)
			{
				// TODO: handle long value explanations, replace each newline with enough spaces,
				// split up long lines
				StringBuilder valueExplanation = new StringBuilder();
				String description = arg.parser().descriptionOfValidValues();
				if(!description.isEmpty())
				{
					boolean isCommand = arg.parser() instanceof Command;
					if(!isCommand)
					{
						if(arg.metaDescription().isEmpty())
						{
							valueExplanation.append("Valid input: ");
						}
						else
						{
							valueExplanation.append(arg.metaDescription().trim() + ": ");
						}
					}

					valueExplanation.append(description);
					valueExplanation.append(Lines.NEWLINE);
					StringsUtil.appendSpaces(indexOfDescriptionColumn, valueExplanation);
				}
				if(arg.isRequired())
					return valueExplanation;

				String descriptionOfDefaultValue = arg.defaultValueDescription();
				if(descriptionOfDefaultValue != null)
				{
					valueExplanation.append("Default: ");
					valueExplanation.append(descriptionOfDefaultValue);
					valueExplanation.append(Lines.NEWLINE);
				}

				return valueExplanation;
			}

			private static final String NAME_SEPARATOR = ", ";
		}

		private static final class NamedArgumentsByFirstName implements Comparator<Argument<?>>, Serializable
		{
			/**
			 * For {@link Serializable}
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public int compare(@Nonnull final Argument<?> one, @Nonnull final Argument<?> two)
			{
				String name = one.names().get(0);
				return name.compareToIgnoreCase(two.names().get(0));
			}
		}
	}

	/**
	 * A container for parsed arguments output by {@link CommandLineParsers#parse(String...)}.
	 * Use {@link #get(Argument)} to fetch the actual command line values.
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
		 * @return the parsed value for the given <code>argumentToFetch</code>,
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
				return false; // Final class so instance of is safe

			ParsedArguments other = (ParsedArguments) obj;
			return holder.isEqualTo(other.holder);
		}
	}

	/**
	 * Holds the currently parsed values
	 */
	@NotThreadSafe
	static final class ParsedArgumentHolder
	{
		@Nonnull final Map<Argument<?>, Object> parsedArguments = newIdentityHashMap();
		@Nonnull final Set<Argument<?>> requiredArgumentsLeft;
		int unnamedArgumentsParsed = 0;

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
			if(!definition.isNamed() && !parsedArguments.containsKey(definition))
			{
				unnamedArgumentsParsed++;
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

	private static final class IgnoringCommandLineParser extends CommandLineParserAdapter
	{
		private static final CommandLineParser INSTANCE = new IgnoringCommandLineParser();
		private static final ParsedArguments EMPTY_RESULT_INSTANCE = new ParsedArguments(new ParsedArgumentHolder(Collections.<Argument<?>>emptySet()));

		@Override
		public String usage(String programName)
		{
			return "Any arguments will be ignored";
		}

		@Override
		public ParsedArguments parse(ListIterator<String> actualArguments) throws ArgumentException
		{
			return EMPTY_RESULT_INSTANCE;
		}
	};

	private abstract static class CommandLineParserAdapter implements CommandLineParser
	{
		@Override
		@Nonnull
		public ParsedArguments parse(@Nonnull final String ... actualArguments) throws ArgumentException
		{
			ListIterator<String> arguments = Arrays.asList(actualArguments).listIterator();
			return parse(arguments);
		}

		@Override
		@Nonnull
		public ParsedArguments parse(@Nonnull final List<String> actualArguments) throws ArgumentException
		{
			return parse(actualArguments.listIterator());
		}
	}
}
