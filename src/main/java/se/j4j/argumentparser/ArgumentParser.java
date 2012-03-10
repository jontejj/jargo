package se.j4j.argumentparser;

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

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.MissingRequiredArgumentException;
import se.j4j.argumentparser.exceptions.UnexpectedArgumentException;
import se.j4j.argumentparser.exceptions.UnhandledRepeatedArgument;
import se.j4j.argumentparser.interfaces.ArgumentHandler;
import se.j4j.argumentparser.internal.TrieTree;
import se.j4j.argumentparser.internal.Usage;
import se.j4j.argumentparser.utils.Lists;

@Immutable
public final class ArgumentParser
{
	/**
	 * Starting point for the call chain:
	 * <pre><code>
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
	 * </code></pre>
	 *
	 * If something goes wrong during the parsing (Missing required arguments, Unexpected arguments, Invalid values),
	 * it will be described by the ArgumentException.
	 *
	 * The error message of ArgumentException ends with the usage text.
	 *
	 * @param argumentDefinitions argument handlers to handle all the given command line arguments, presumably produced with {@link ArgumentFactory} or with your own disciples of {@link Argument}
	 * @return an ArgumentParser which you can call {@link ArgumentParser#parse(String...)} on and get {@link ParsedArguments} out of.
	 * @throws IllegalArgumentException if two or more of the given arguments uses the same name (either short or long name)
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentParser forArguments(final @Nonnull Argument<?> ...argumentDefinitions)
	{
		return new ArgumentParser(argumentDefinitions);
	}

	private ArgumentParser(final @Nonnull Argument<?> ...argumentDefinitions)
	{
		namedArguments = new HashMap<String, Argument<?>>(argumentDefinitions.length);
		indexedArgumentDefinitions = new ArrayList<Argument<?>>(argumentDefinitions.length);
		specialSeparatorArguments = TrieTree.newTree();
		ignoreCaseArguments = new HashMap<String, Argument<?>>();
		ignoreCaseSpecialSeparatorArguments = TrieTree.newTree();
		requiredArguments = new HashSet<Argument<?>>(argumentDefinitions.length);
		propertyMapArguments = TrieTree.newTree();
		ignoreCasePropertyMapArguments = TrieTree.newTree();
		allArguments = new HashSet<Argument<?>>(argumentDefinitions.length);
		for(Argument<?> arg : argumentDefinitions)
		{
			addArgument(arg);
		}
	}

	/**
	 * A list where arguments created without names is put
	 */
	private final @Nonnull List<Argument<?>> indexedArgumentDefinitions;

	/**
	 * A map containing both short-named and long-named arguments
	 */
	private final @Nonnull Map<String, Argument<?>> namedArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s
	 */
	private final @Nonnull TrieTree<Argument<?>> specialSeparatorArguments;

	/**
	 * Map for arguments that's {@link Argument#isIgnoringCase()}. Stores it's keys with lower case.
	 */
	private final @Nonnull Map<String, Argument<?>> ignoreCaseArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s and {@link Argument#isIgnoringCase()}
	 */
	private final @Nonnull TrieTree<Argument<?>> ignoreCaseSpecialSeparatorArguments;

	private final @Nonnull TrieTree<Argument<?>> propertyMapArguments;

	private final @Nonnull TrieTree<Argument<?>> ignoreCasePropertyMapArguments;

	/**
	 * If arguments are required, set by calling {@link Argument#required()}, and they aren't given on the command line,
	 * {@link MissingRequiredArgumentException} is thrown when {@link #parse(String...)} is called.
	 * TODO: should parse handle this printout itself instead of throwing?
	 */
	private final @Nonnull Set<Argument<?>> requiredArguments;

	private final @Nonnull Set<Argument<?>> allArguments;

	private void addArgument(final @Nonnull Argument<?> argument)
	{
		if(argument.isNamed())
		{
			for(String name : argument.names())
			{
				putArgument(name, argument);
			}
		}
		else
		{
			indexedArgumentDefinitions.add(argument);
		}
		if(argument.isRequired())
		{
			requiredArguments.add(argument);
		}
		allArguments.add(argument);
	}

	private void putArgument(final @Nonnull String key, final @Nonnull Argument<?> argument)
	{
		Argument<?> oldHandler = null;
		String separator = argument.separator();
		if(argument.isPropertyMap())
		{
			oldHandler = propertyMapArguments.set(key, argument);
			if(argument.isIgnoringCase())
			{
				ignoreCasePropertyMapArguments.set(key.toLowerCase(), argument);
			}
		}
		else if(separator != null)
		{
			oldHandler = specialSeparatorArguments.set(key + separator, argument);
			if(argument.isIgnoringCase())
			{
				ignoreCaseSpecialSeparatorArguments.set((key + separator).toLowerCase(), argument);
			}
		}
		else
		{
			oldHandler = namedArguments.put(key, argument);
			if(argument.isIgnoringCase())
			{
				//TODO: as ignoreCase may match some arguments in namedArguments
				//that it shouldn't we need to loop over every argument and check for duplicates
				ignoreCaseArguments.put(key.toLowerCase(), argument);
			}
		}
		if(oldHandler != null)
		{
			throw new IllegalArgumentException(argument + " handles the same argument as: " + oldHandler);
		}
	}

	public ParsedArguments parse(final @Nonnull String... actualArguments) throws ArgumentException
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
		actualArguments = Lists.copy(actualArguments); //specialSeparatorArguments, MapArgument etc may modify the list

		Map<ArgumentHandler<?>, Object> parsedArguments = new IdentityHashMap<ArgumentHandler<?>, Object>();
		Set<Argument<?>> requiredArgumentsLeft = new HashSet<Argument<?>>(requiredArguments);

		try
		{
			int indexedPosition = 0;
			while(actualArguments.hasNext())
			{
				Argument<?> argumentDefinition = getHandlerForArgument(actualArguments, indexedPosition);
				if(argumentDefinition != null)
				{
					if(!argumentDefinition.isNamed())
					{
						indexedPosition++;
					}
					ArgumentHandler<Object> handler = (ArgumentHandler<Object>) argumentDefinition.handler();
					Object oldValue = parsedArguments.get(handler);
					try
					{
						Object parsedValue = handler.parse(actualArguments, oldValue, argumentDefinition);
						argumentDefinition.validate(parsedValue);
						parsedArguments.put(handler, parsedValue);
						if(oldValue != null && !argumentDefinition.isAllowedToRepeat() && !argumentDefinition.isPropertyMap())
						{
							throw UnhandledRepeatedArgument.create(argumentDefinition);
						}
					}
					catch(ArgumentException e)
					{
						//Rethrow with more information
						throw e.errorneousArgument(argumentDefinition);
					}
					if(argumentDefinition.isRequired())
					{
						requiredArgumentsLeft.remove(argumentDefinition);
					}
				}
			}
			if(!requiredArgumentsLeft.isEmpty())
			{
				throw MissingRequiredArgumentException.create(requiredArgumentsLeft);
			}
		}
		catch(ArgumentException parserError)
		{
			parserError.setOriginParser(this);
			throw parserError;
		}
		return new ParsedArguments(parsedArguments);
	}

	private Argument<?> getHandlerForArgument(final @Nonnull ListIterator<String> actualArguments, final int indexedPosition) throws ArgumentException
	{
		String currentArgument = actualArguments.next();
		Argument<?> argumentHandler = namedArguments.get(currentArgument);

		if(argumentHandler != null)
		{
			return argumentHandler;
		}
		String lowerCase = currentArgument.toLowerCase();

		argumentHandler = ignoreCaseArguments.get(lowerCase);
		if(argumentHandler != null)
		{
			return argumentHandler;
		}
		argumentHandler = propertyMapArguments.getLastMatch(currentArgument);
		if(argumentHandler == null)
		{
			argumentHandler = ignoreCasePropertyMapArguments.getLastMatch(lowerCase);
		}
		if(argumentHandler != null)
		{
			actualArguments.previous();
			return argumentHandler;
		}
		argumentHandler = ignoreCaseSpecialSeparatorArguments.getLastMatch(lowerCase);
		if(argumentHandler == null)
		{
			argumentHandler = specialSeparatorArguments.getLastMatch(currentArgument);
		}

		if(argumentHandler != null)
		{
			//Remove "name=" from "name=value"
			actualArguments.set(currentArgument.substring(1 + currentArgument.indexOf(argumentHandler.separator())));
			actualArguments.previous();
		}
		else
		{
			if(indexedPosition >= indexedArgumentDefinitions.size())
			{
				/**
				 * TODO: handle "-fs" as well as "-f -s"
				 * TODO: maybe handle properties like arguments: -Dproperty.name=value
				 */
				throw UnexpectedArgumentException.unexpectedArgument(currentArgument);
				//TODO: suggest alternative options/parameters based on the faulty characters' distance (keyboard wise (consider dvorak))
				//Ask Did you mean and provide y/n
				//Simply ignore dropped argument
				//return null;
			}
			argumentHandler = indexedArgumentDefinitions.get(indexedPosition);
			actualArguments.previous();
		}
		return argumentHandler;
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
	 * A container for parsed arguments output by {@link ArgumentParser#parse(String...)}.
	 * Use {@link #get(Argument)} to fetch the actual command line values.
	 */
	@Immutable
	public static final class ParsedArguments
	{
		private final @Nonnull Map<ArgumentHandler<?>, ?> parsedArguments;

		private ParsedArguments(final @Nonnull Map<ArgumentHandler<?>, ?> parsedArguments)
		{
			this.parsedArguments = parsedArguments;
		}

		/**
		 * @param argumentToFetch
		 * @return the parsed value for the given <code>argumentToFetch</code>, if no value was given on the command line and the argument isn't {@link Argument#required()} the {@link Argument#defaultValue()} is returned.
		 */
		@Nullable
		public <T> T get(final @Nonnull Argument<T> argumentToFetch)
		{
			@SuppressWarnings("unchecked") //Safe because ArgumentHolder#parse(...) guarantees that the map is heterogeneous
			T value = (T) parsedArguments.get(argumentToFetch.handler());
			if(value == null)
			{
				return argumentToFetch.defaultValue();
			}
			return value;
		}

		@Override
		public String toString()
		{
			return parsedArguments.toString();
		}
	}
}
