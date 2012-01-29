package se.j4j.argumentparser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import jjonsson.weather_notifier.TrieTree;


public class ArgumentParser
{
	/**
	 * Starting point for the call chain:
	 * <pre><code>
	 * import static se.j4j.argumentparser.ArgumentFactory.* (or specific argument types)
	 * ...
	 * String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};
	 *
	 * Argument&lt;Boolean&gt; enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out");
	 * Argument&lt;Integer&gt; port = integerArgument("-p", "--listen-port").defaultValue(8080).description("The port to start the server on.");
	 * Argument&lt;String&gt; greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with");
	 *
	 * ParsedArguments arguments = ArgumentParser.forArguments(greetingPhrase, enableLogging, port).parse(args);
	 *
	 * assertTrue(enableLogging + " was not found in parsed arguments", arguments.get(enableLogging));
	 * </code></pre>
	 *
	 * @param argumentDefinitions argument handlers to handle all the given command line arguments, presumably produced with {@link ArgumentFactory} or with your own disciples of {@link Argument}
	 * @return an ArgumentParser which you can call {@link ArgumentParser#parse(String...)} on and get {@link ParsedArguments} out of.
	 * @throws IllegalArgumentException if two or more of the given arguments uses the same name (either short or long name)
	 */
	public static ArgumentParser forArguments(final Argument<?> ...argumentDefinitions)
	{
		return new ArgumentParser(argumentDefinitions);
	}

	private ArgumentParser(final Argument<?> ...argumentDefinitions)
	{
		namedArguments = new HashMap<String, Argument<?>>(argumentDefinitions.length);
		indexedArgumentDefinitions = new ArrayList<Argument<?>>(argumentDefinitions.length);
		specialSeparatorArguments = TrieTree.newTree();
		ignoreCaseArguments = new HashMap<String, Argument<?>>();
		ignoreCaseSpecialSeparatorArguments = TrieTree.newTree();
		requiredArguments = new HashSet<Argument<?>>(argumentDefinitions.length);
		for(Argument<?> arg : argumentDefinitions)
		{
			addArgument(arg);
		}
	}

	//TODO: add usage() method (for CommandParser as well)

	/**
	 * A list where arguments created without names is put
	 */
	private final List<Argument<?>> indexedArgumentDefinitions;

	/**
	 * A map containing both short-named and long-named arguments
	 */
	private final Map<String, Argument<?>> namedArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s
	 */
	private final TrieTree<Argument<?>> specialSeparatorArguments;

	/**
	 * Map for arguments that's {@link Argument#isIgnoringCase()}. Stores it's keys with lower case.
	 */
	private final Map<String, Argument<?>> ignoreCaseArguments;

	/**
	 * A map with arguments that has special {@link Argument#separator()}s and {@link Argument#isIgnoringCase()}
	 */
	private final TrieTree<Argument<?>> ignoreCaseSpecialSeparatorArguments;

	/**
	 * If arguments are required, set by calling {@link Argument#required()}, and they aren't given on the command line,
	 * {@link MissingRequiredArgumentException} is thrown when {@link #parse(String...)} is called.
	 * TODO: should parse handle this printout itself instead of throwing?
	 */
	private final Set<Argument<?>> requiredArguments;

	private void addArgument(final Argument<?> argument)
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
	}

	private void putArgument(final String key, final Argument<?> argument)
	{
		Argument<?> oldHandler = null;
		String separator = argument.separator();
		if(separator != null)
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

	/**
	 * Set by {@link #throwOnUnexpectedArgument()} and can be used to be strict about what's given as arguments, defaults to false.
	 */
	private boolean throwOnUnexpectedArgument = false;


	public ArgumentParser throwOnUnexpectedArgument()
	{
		throwOnUnexpectedArgument = true;
		return this;
	}

	public ParsedArguments parse(final String... actualArguments) throws ArgumentException
	{
		ListIterator<String> arguments = Arrays.asList(actualArguments).listIterator();
		return parse(arguments);
	}

	public ParsedArguments parse(final List<String> actualArguments) throws ArgumentException
	{
		return parse(actualArguments.listIterator());
	}

	/**
	 *
	 * @param actualArguments
	 * @return
	 * @throws ArgumentException
	 * @throws MissingRequiredArgumentException
	 */
	public ParsedArguments parse(ListIterator<String> actualArguments) throws ArgumentException
	{
		//TODO: how should this copy be made? It's made because specialSeparatorArguments modifies the list
		List<String> listCopy = new ArrayList<String>();
		while(actualArguments.hasNext())
		{
			listCopy.add(actualArguments.next());
		}
		actualArguments = listCopy.listIterator();

		Map<ArgumentHandler<?>, Object> parsedArguments = new IdentityHashMap<ArgumentHandler<?>, Object>();
		Set<Argument<?>> requiredArgumentsLeft = new HashSet<Argument<?>>(requiredArguments);
		int indexedPosition = 0;
		while(actualArguments.hasNext())
		{
			Argument<?> argumentHandler = getHandlerForArgument(actualArguments, indexedPosition);
			if(argumentHandler != null)
			{
				if(!argumentHandler.isNamed())
				{
					indexedPosition++;
				}
				try
				{
					ArgumentHandler<?> actualHandler = argumentHandler.handler();
					if(actualHandler instanceof RepeatedArgument)
					{
						parsedArguments.put(actualHandler, ((RepeatedArgument<?>) actualHandler).parseRepeated(actualArguments, parsedArguments));
					}
					else
					{
						//TODO: before parse(...) provide a pre/post validator interface to validate values
						Object oldValue = parsedArguments.put(actualHandler, actualHandler.parse(actualArguments));
						if(oldValue != null)
						{
							throw UnhandledRepeatedArgument.create(argumentHandler);
						}
					}
					if(argumentHandler.isRequired())
					{
						requiredArgumentsLeft.remove(argumentHandler);
					}
				}
				catch(NoSuchElementException argumentMissingException)
				{
					throw ArgumentException.create(ArgumentExceptionCodes.MISSING_PARAMETER).
					errorneousArgument(argumentHandler).initCause(argumentMissingException);
				}
			}
		}
		if(!requiredArgumentsLeft.isEmpty())
		{
			throw MissingRequiredArgumentException.create(requiredArgumentsLeft);
		}
		return new ParsedArguments(parsedArguments);
	}

	private Argument<?> getHandlerForArgument(final ListIterator<String> actualArguments, final int indexedPosition) throws ArgumentException
	{
		String currentArgument = actualArguments.next();
		Argument<?> argumentHandler = namedArguments.get(currentArgument);

		if(argumentHandler != null)
		{
			return argumentHandler;
		}

		argumentHandler = ignoreCaseArguments.get(currentArgument.toLowerCase());
		if(argumentHandler != null)
		{
			return argumentHandler;
		}

		argumentHandler = ignoreCaseSpecialSeparatorArguments.getLastMatch(currentArgument.toLowerCase());
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
				if(throwOnUnexpectedArgument)
				{
					throw ArgumentException.create(ArgumentExceptionCodes.UNHANDLED_PARAMETER);
				}
				//TODO: suggest alternative options/parameters based on the faulty characters' distance (keyboard wise (consider dvorak))
				//Ask Did you mean and provide y/n
				//Simply ignore dropped argument
				return null;
			}
			argumentHandler = indexedArgumentDefinitions.get(indexedPosition);
			actualArguments.previous();
		}
		return argumentHandler;
	}

	/**
	 * A container for parsed arguments output by {@link ArgumentParser#parse(String...)}.
	 * Use {@link #get(Argument)} to fetch the actual command line values.
	 *
	 * @author jonatanjoensson
	 *
	 */
	public static final class ParsedArguments
	{
		private final Map<ArgumentHandler<?>, ?> parsedArguments;

		private ParsedArguments(final Map<ArgumentHandler<?>, ?> parsedArguments)
		{
			this.parsedArguments = parsedArguments;
		}

		/**
		 * @param argumentToFetch
		 * @return the parsed value for the given <code>argumentToFetch</code>, if no value was given on the command line and the argument isn't {@link Argument#required()} the {@link Argument#defaultValue()} is returned.
		 */
		public <T> T get(final Argument<T> argumentToFetch)
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
