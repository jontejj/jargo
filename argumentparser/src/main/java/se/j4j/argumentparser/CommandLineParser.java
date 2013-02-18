package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.argumentparser.ArgumentFactory.command;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.Lists;

/**
 * Manages multiple {@link Argument}s and/or {@link Command}s. The brain of this API.
 * Its primary goal is to decide which {@link Argument} each input string belongs to.
 * Immutability is dearly embraced. Thus a {@link CommandLineParser} can be reused to parse
 * arguments over and over again. Different {@link CommandLineParser}s can even
 * share {@link Argument} configurations as {@link Argument} instances are immutable as well.
 * Documentation through example:
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * import static se.j4j.argumentparser.ArgumentFactory.*;
 * ...
 * String[] args = {"--enable-logging", "--listen-port", "8090", "Hello"};
 * 
 * Argument&lt;Boolean&gt; enableLogging = optionArgument("-l", "--enable-logging").description("Output debug information to standard out").build();
 * Argument&lt;String&gt; greetingPhrase = stringArgument().description("A greeting phrase to greet new connections with").build();
 * Argument&lt;List&lt;Integer&gt;&gt; port = integerArgument("-p", "--listen-port")
 * 						.defaultValue(8080)
 * 						.description("The port clients should connect to.")
 * 						.metaDescription("&lt;port&gt;")
 * 						.limitTo(Ranges.closed(0, 65536)
 * 						.repeated().build();
 * 
 * try
 * {
 *   ParsedArguments arguments = CommandLineParser.withArguments(greetingPhrase, enableLogging, port).parse(args);
 *   assertThat(arguments.get(enableLogging)).isTrue();
 *   assertThat(arguments.get(port)).isEqualTo(Arrays.asList(8090));
 *   assertThat(arguments.get(greetingPhrase)).isEqualTo("Hello");
 * }
 * catch(ArgumentException exception)
 * {
 *   System.out.println(exception.getMessageAndUsage());
 *   System.exit(1);
 * }
 * </code>
 * </pre>
 * 
 * <pre>
 * For this program the usage would look like ("YourProgramName" is fetched from stack traces by default):
 * <code>
 * Usage: YourProgramName [Arguments]
 * 
 * Arguments:
 * &lt;string&gt;                       A greeting phrase to greet new connections with
 *                                &lt;string&gt;: any string
 *                                Default:
 * -l, --enable-logging           Output debug information to standard out
 *                                Default: disabled
 * -p, --listen-port &lt;port&gt;       The port clients should connect to [Supports Multiple occurrences]
 *                                port: [0‥65536]
 *                                Default: 8080
 * </code>
 * If something goes wrong during the parsing (Missing required arguments, Unexpected arguments, Invalid values),
 * it will be described by the ArgumentException. Use {@link ArgumentException#getMessageAndUsage()} if you
 * want to explain what went wrong to the user.
 * </pre>
 * 
 * <b>Thread safety concerns:</b> If there is a parsing occurring while any modifying method is
 * invoked, such as {@link #programName(String)}, the changes won't be seen until the start of the
 * next parse invocation. Also, if the goal is to set {@link #programName(String)} and
 * {@link #programDescription(String)} atomically you'll have to synchronize such changes
 * externally.
 */
@ThreadSafe
public final class CommandLineParser
{
	// Internally CommandLineParser is a builder for CommandLineParserInstance but the idea is to
	// keep this idea hidden in the API to lessen the API complexity
	@GuardedBy("modifyGuard") private final List<Argument<?>> argumentDefinitions;
	@GuardedBy("modifyGuard") private final AtomicReference<ProgramInformation> programInformation = new AtomicReference<ProgramInformation>(ProgramInformation.AUTO);

	private final AtomicReference<Locale> locale = new AtomicReference<Locale>(Locale.getDefault());

	@GuardedBy("modifyGuard") private final AtomicReference<CommandLineParserInstance> cachedParser;

	private final Lock modifyGuard = new ReentrantLock();

	/**
	 * Creates a {@link CommandLineParser} with support for the given {@code argumentDefinitions}.
	 * {@link Command}s can be added later with {@link #and(Command)}.
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
		return new CommandLineParser(argumentDefinitions);
	}

	/**
	 * Creates a {@link CommandLineParser} with support for {@code commands}. To add additional
	 * {@link Argument}s or {@link Command}s there is {@link #and(Argument)} and
	 * {@link #and(Command)}.
	 * 
	 * @param commands the commands to support initially
	 * @return a CommandLineParser which you can call {@link CommandLineParser#parse(String...)} on
	 *         and get {@link ParsedArguments} out of.
	 * @see CommandLineParser
	 */
	@CheckReturnValue
	@Nonnull
	public static CommandLineParser withCommands(final Command ... commands)
	{
		List<Argument<?>> commandsAsArguments = Lists.newArrayListWithExpectedSize(commands.length);
		for(Command c : commands)
		{
			commandsAsArguments.add(command(c).build());
		}
		return new CommandLineParser(commandsAsArguments);
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
		return parser().parse(Lists.newArrayList(actualArguments), locale());
	}

	/**
	 * {@link Iterable} version of {@link #parse(String...)}
	 */
	@Nonnull
	public ParsedArguments parse(final Iterable<String> actualArguments) throws ArgumentException
	{
		return parser().parse(Lists.newArrayList(actualArguments), locale());
	}

	/**
	 * Returns a usage text describing all arguments this {@link CommandLineParser} handles.
	 * Suitable to print on {@link System#out}.
	 */
	@CheckReturnValue
	@Nonnull
	public String usage()
	{
		// TODO: return Usage instead? more future proof...
		return new Usage(parser().allArguments(), locale(), programInformation()).toString();
	}

	/**
	 * Adds support for {@code commandToAlsoSupport} in this {@link CommandLineParser}. Typically
	 * used in a chained fashion when faced with many supported {@link Command}s.
	 * 
	 * @param commandToAlsoSupport the command to add support for
	 * @return this {@link CommandLineParser} to allow for chained calls
	 */
	@CheckReturnValue
	@Nonnull
	public CommandLineParser and(final Command commandToAlsoSupport)
	{
		verifiedAdd(command(commandToAlsoSupport).build());
		return this;
	}

	/**
	 * Adds support for {@code argumentToAlsoSupport} in this {@link CommandLineParser}. Typically
	 * used in a chained fashion when faced with many supported arguments.
	 * 
	 * @param argumentToAlsoSupport the argument to add support for
	 * @return this {@link CommandLineParser} to allow for chained calls
	 */
	@CheckReturnValue
	@Nonnull
	public CommandLineParser and(Argument<?> argumentToAlsoSupport)
	{
		// TODO: test thread safety, sleep long in a parser and, add argument and verify that the
		// second parse uses the added argument
		verifiedAdd(argumentToAlsoSupport);
		return this;
	}

	/**
	 * Verify that the parser would be usable after adding {@code argumentToCheck} to it.
	 * This allows future parse operations to still use the unaffected old parser.
	 */
	private void verifiedAdd(Argument<?> argumentToAdd)
	{
		try
		{
			modifyGuard.lock();
			List<Argument<?>> newDefinitions = Lists.newArrayList(argumentDefinitions);
			newDefinitions.add(checkNotNull(argumentToAdd));
			cachedParser.set(new CommandLineParserInstance(newDefinitions, programInformation()));
			// Everything went fine, accept argument
			argumentDefinitions.add(argumentToAdd);
		}
		finally
		{
			modifyGuard.unlock();
		}
	}

	/**
	 * Sets the {@code programName} to print with {@link #usage()}
	 * 
	 * @return this parser
	 */
	public CommandLineParser programName(String programName)
	{
		try
		{
			modifyGuard.lock();
			programInformation.set(programInformation().programName(programName));
			cachedParser.set(new CommandLineParserInstance(argumentDefinitions, programInformation()));
		}
		finally
		{
			modifyGuard.unlock();
		}
		return this;
	}

	/**
	 * Sets the {@code programDescription} to print with {@link #usage()}
	 * 
	 * @return this parser
	 */
	public CommandLineParser programDescription(String programDescription)
	{
		try
		{
			modifyGuard.lock();
			programInformation.set(programInformation().programDescription(programDescription));
			cachedParser.set(new CommandLineParserInstance(argumentDefinitions, programInformation()));
		}
		finally
		{
			modifyGuard.unlock();
		}
		return this;
	}

	/**
	 * {@link Locale#getDefault()} is dangerous to use. It's a global and mutable variable
	 * which makes it hard to give any guarantees on what it will be at a certain time.
	 * It's much preferred to set the {@link Locale} to parse with/print usage in with this method
	 * instead.
	 * To use a different {@link Locale} for one of your {@link Argument}s you can override this
	 * with {@link ArgumentBuilder#locale(Locale)}.
	 * 
	 * @param localeToUse the {@link Locale} to parse input strings with (it will be passed to
	 *            {@link StringParser#parse(String, Locale)} and
	 *            {@link StringParser#descriptionOfValidValues(Locale)})
	 * @return this parser
	 */
	public CommandLineParser locale(Locale localeToUse)
	{
		locale.set(checkNotNull(localeToUse));
		return this;
	}

	/**
	 * Returns the {@link #usage()} for this {@link CommandLineParser}
	 */
	@Override
	public String toString()
	{
		return usage();
	};

	CommandLineParser(Iterable<Argument<?>> argumentDefinitions)
	{
		this.argumentDefinitions = Lists.newCopyOnWriteArrayList(argumentDefinitions);
		this.cachedParser = new AtomicReference<CommandLineParserInstance>(new CommandLineParserInstance(this.argumentDefinitions,
				programInformation()));
	}

	CommandLineParserInstance parser()
	{
		return cachedParser.get();
	}

	Locale locale()
	{
		return locale.get();
	}

	ProgramInformation programInformation()
	{
		return programInformation.get();
	}
}
