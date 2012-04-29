package se.j4j.argumentparser;

import static se.j4j.argumentparser.utils.ListUtil.copy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.exceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.internal.TrieTree;
import se.j4j.argumentparser.internal.Usage;

@Immutable
public final class ArgumentParser
{
	/**
	 * Starting point for the call chain:
	 * 
	 * <pre>
	 * <code>
	 * import static se.j4j.argumentparser.ArgumentFactory.* (or specific argument types)
	 * ...
	 * String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};
	 * 
	 * Argument&lt;Boolean&gt; enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out");
	 * Argument&lt;Integer&gt; port = integerArgument("-p", "--listen-port").defaultValue(8080).description("The port clients should connect to.");
	 * Argument&lt;String&gt; greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with");
	 * 
	 * try
	 * {
	 * 	ParsedArguments arguments = ArgumentParser.forArguments(greetingPhrase, enableLogging, port).parse(args);
	 * }
	 * catch(ArgumentException exception)
	 * {
	 * 	System.out.println(exception.getMessageAndUsage());
	 * 	System.exit(1);
	 * }
	 * 
	 * assertTrue(enableLogging + " was not found in parsed arguments", arguments.get(enableLogging));
	 * </code>
	 * </pre>
	 * 
	 * If something goes wrong during the parsing (Missing required arguments,
	 * Unexpected arguments, Invalid values),
	 * it will be described by the ArgumentException.
	 * The error message of ArgumentException ends with the usage text.
	 * 
	 * @param argumentDefinitions argument handlers to handle all the given
	 *            command line arguments, presumably produced with
	 *            {@link ArgumentFactory} or with your own disciples of
	 *            {@link ArgumentBuilder}
	 * @return an ArgumentParser which you can call
	 *         {@link ArgumentParser#parse(String...)} on and get
	 *         {@link ParsedArguments} out of.
	 * @throws IllegalArgumentException if two or more of the given arguments
	 *             uses the same name (either short or long name)
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentParser forArguments(final @Nonnull List<Argument<?>> argumentDefinitions)
	{
		return new ArgumentParser(argumentDefinitions);
	}

	/**
	 * See {@link #forArguments(List)}
	 * 
	 * @param argumentDefinitions
	 * @return
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentParser forArguments(final @Nonnull Argument<?> ... argumentDefinitions)
	{
		return new ArgumentParser(Arrays.asList(argumentDefinitions));
	}

	private ArgumentParser(final @Nonnull List<Argument<?>> argumentDefinitions)
	{
		namedArguments = new HashMap<String, Argument<?>>(argumentDefinitions.size());
		indexedArguments = new ArrayList<Argument<?>>(argumentDefinitions.size());
		specialSeparatorArguments = TrieTree.newTree();
		ignoreCaseArguments = new HashMap<String, Argument<?>>();
		ignoreCaseSpecialSeparatorArguments = TrieTree.newTree();
		requiredArguments = new HashSet<Argument<?>>(argumentDefinitions.size());
		propertyMapArguments = TrieTree.newTree();
		ignoreCasePropertyMapArguments = TrieTree.newTree();
		allArguments = new HashSet<Argument<?>>(argumentDefinitions.size());
		for(Argument<?> definition : argumentDefinitions)
		{
			addArgumentDefinition(definition);
		}
	}

	/**
	 * A list where arguments created without names is put
	 */
	private final @Nonnull List<Argument<?>> indexedArguments;

	/**
	 * A map containing both short-named and long-named arguments
	 */
	private final @Nonnull Map<String, Argument<?>> namedArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s
	 */
	private final @Nonnull TrieTree<Argument<?>> specialSeparatorArguments;

	/**
	 * Map for arguments that's {@link Argument#isIgnoringCase()}.
	 * Stores it's keys with lower case.
	 */
	private final @Nonnull Map<String, Argument<?>> ignoreCaseArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s and
	 * {@link Argument#isIgnoringCase()}
	 */
	private final @Nonnull TrieTree<Argument<?>> ignoreCaseSpecialSeparatorArguments;

	private final @Nonnull TrieTree<Argument<?>> propertyMapArguments;

	private final @Nonnull TrieTree<Argument<?>> ignoreCasePropertyMapArguments;

	/**
	 * If arguments are required, set by calling {@link Argument#required()},
	 * and they aren't given on the command line,
	 * {@link MissingRequiredArgumentException} is thrown when
	 * {@link #parse(String...)} is called.
	 * TODO: should parse handle this printout itself instead of throwing?
	 */
	private final @Nonnull Set<Argument<?>> requiredArguments;

	private final @Nonnull Set<Argument<?>> allArguments;

	private void addArgumentDefinition(final @Nonnull Argument<?> definition)
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

	private void addNamedArgumentDefinition(final @Nonnull String key, final @Nonnull Argument<?> definition)
	{
		Argument<?> oldDefinition = null;
		String separator = definition.separator();
		if(definition.isPropertyMap())
		{
			oldDefinition = propertyMapArguments.set(key, definition);
			if(definition.isIgnoringCase())
			{
				ignoreCasePropertyMapArguments.set(key.toLowerCase(), definition);
			}
		}
		else if(separator != null)
		{
			oldDefinition = specialSeparatorArguments.set(key + separator, definition);
			if(definition.isIgnoringCase())
			{
				ignoreCaseSpecialSeparatorArguments.set((key + separator).toLowerCase(), definition);
			}
		}
		else
		{
			oldDefinition = namedArguments.put(key, definition);
			if(definition.isIgnoringCase())
			{
				// TODO: as ignoreCase may match some arguments in
				// namedArguments
				// that it shouldn't, we need to loop over every argument and
				// check for duplicates
				ignoreCaseArguments.put(key.toLowerCase(), definition);
			}
		}
		if(oldDefinition != null)
			throw new IllegalArgumentException(definition + " handles the same argument as: " + oldDefinition);
	}

	public ParsedArguments parse(final @Nonnull String ... actualArguments) throws ArgumentException
	{
		ListIterator<String> arguments = Arrays.asList(actualArguments).listIterator();
		return parse(arguments);
	}

	public ParsedArguments parse(final @Nonnull List<String> actualArguments) throws ArgumentException
	{
		return parse(actualArguments.listIterator());
	}

	public ParsedArguments parse(@Nonnull ListIterator<String> actualArguments) throws ArgumentException
	{
		actualArguments = copy(actualArguments); // specialSeparatorArguments,
													// MapArgument etc may
													// modify the list

		ParsedArgumentHolder holder = new ParsedArgumentHolder();
		while(actualArguments.hasNext())
		{
			parseArgument(actualArguments, holder);
		}

		for(Argument<?> arg : holder.parsedArguments.keySet())
		{
			finalizeArgument(arg, holder);
			validateArgument(arg, holder);
		}

		try
		{
			return new ParsedArguments(holder);
		}
		catch(ArgumentException e)
		{
			e.setOriginParser(this);
			// Re throw with more information
			throw e;
		}
	}

	private void parseArgument(final ListIterator<String> actualArguments, final ParsedArgumentHolder holder) throws ArgumentException

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
			// Re throw with more information
			throw e.errorneousArgument(definition);
		}
	}

	private <T> void parseArgument(final ListIterator<String> actualArguments, final ParsedArgumentHolder parsedArguments,
			final Argument<T> definition) throws ArgumentException
	{
		T oldValue = parsedArguments.getValue(definition);

		if(oldValue != null && !definition.isAllowedToRepeat() && !definition.isPropertyMap())
			throw UnhandledRepeatedArgument.create(definition);

		ArgumentHandler<T> handler = definition.handler();

		T parsedValue = handler.parse(actualArguments, oldValue, definition);

		parsedArguments.put(definition, parsedValue);
	}

	/**
	 * @param actualArguments
	 * @param holder
	 * @return a definition that defines how to handle an argument
	 * @throws UnexpectedArgumentException if no definition could be found for
	 *             the current argument
	 */
	@Nonnull
	private Argument<?> getDefinitionForCurrentArgument(final @Nonnull ListIterator<String> actualArguments, final ParsedArgumentHolder holder)
			throws UnexpectedArgumentException
	{
		String currentArgument = actualArguments.next();
		Argument<?> definition = namedArguments.get(currentArgument);

		if(definition != null)
			return definition;
		String lowerCase = currentArgument.toLowerCase();

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
				// faulty characters' distance (keyboard wise (consider dvorak))
				// Ask Did you mean and provide y/n
				throw UnexpectedArgumentException.unexpectedArgument(actualArguments);

			definition = indexedArguments.get(holder.unnamedArgumentsParsed);
			actualArguments.previous();
		}
		return definition;
	}

	@Override
	public String toString()
	{
		return Usage.forArguments("<main class>", allArguments).toString();
	}

	/**
	 * Returns a Usage instance describing this ArgumentParser
	 */
	@CheckReturnValue
	@Nonnull
	public Usage usage(final @Nonnull String programName)
	{
		return Usage.forArguments(programName, allArguments);
	}

	/**
	 * A container for parsed arguments output by
	 * {@link ArgumentParser#parse(String...)}.
	 * Use {@link #get(Argument)} to fetch the actual command line values.
	 */
	@Immutable
	public static final class ParsedArguments
	{
		private final @Nonnull ParsedArgumentHolder parsedArguments;

		private ParsedArguments(final @Nonnull ParsedArgumentHolder parsedArguments) throws ArgumentException
		{
			if(!parsedArguments.requiredArgumentsLeft.isEmpty())
				throw MissingRequiredArgumentException.create(parsedArguments.requiredArgumentsLeft);

			this.parsedArguments = parsedArguments;

			for(Argument<?> arg : parsedArguments.parsedArguments.keySet())
			{
				parsedArgument(arg);
			}
		}

		private <T> void parsedArgument(Argument<T> arg)
		{
			arg.parsedValue(get(arg));
		}

		/**
		 * @param argumentToFetch
		 * @return the parsed value for the given <code>argumentToFetch</code>,
		 *         if no value was given on the command line the
		 *         {@link Argument#defaultValue()} is returned.
		 */
		@Nullable
		@CheckReturnValue
		public <T> T get(final @Nonnull Argument<T> argumentToFetch)
		{
			T value = parsedArguments.getValue(argumentToFetch);
			if(value == null)
				return argumentToFetch.defaultValue();

			return value;
		}

		@Override
		public String toString()
		{
			return parsedArguments.toString();
		}

		@Override
		public int hashCode()
		{
			return 31 + parsedArguments.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			ParsedArguments other = (ParsedArguments) obj;
			return parsedArguments.equals(other.parsedArguments);
		}
	}

	/**
	 * Holds the currently parsed values
	 */
	@NotThreadSafe
	public final class ParsedArgumentHolder
	{
		final @Nonnull Map<Argument<?>, Object> parsedArguments = new IdentityHashMap<Argument<?>, Object>();
		final @Nonnull Set<Argument<?>> requiredArgumentsLeft = new HashSet<Argument<?>>(requiredArguments);
		int unnamedArgumentsParsed = 0;

		ParsedArgumentHolder()
		{
		}

		public <T> void put(final @Nonnull Argument<T> definition, final @Nullable T value)
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

		// Safe because put guarantees that the map is heterogeneous
		@SuppressWarnings("unchecked")
		<T> T getValue(final @Nonnull Argument<T> definition)
		{
			Object value = parsedArguments.get(definition);
			return (T) value;
		}

		@Override
		public String toString()
		{
			return parsedArguments.toString();
		}

		@Override
		public int hashCode()
		{
			return 31 + parsedArguments.hashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			ParsedArgumentHolder other = (ParsedArgumentHolder) obj;
			return parsedArguments.equals(other.parsedArguments);
		}
	}

	private <T> void finalizeArgument(Argument<T> arg, ParsedArgumentHolder holder)
	{
		arg.finalizeValue(holder.getValue(arg), holder);
	}

	private <T> void validateArgument(Argument<T> arg, ParsedArgumentHolder holder) throws InvalidArgument
	{
		arg.validate(holder.getValue(arg));
	}
}
