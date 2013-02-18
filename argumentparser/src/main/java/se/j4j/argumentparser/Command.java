package se.j4j.argumentparser;

import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParserInstance.ArgumentIterator;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.strings.Description;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

/**
 * <pre>
 * {@link Command}s automatically gets an invocation of execute when given on the command line.
 * This is particularly useful to avoid a never ending switch statement.
 * 
 * {@link Command}s have a {@link CommandLineParser} themselves (and thereby sub-commands are allowed as well), that is,
 * they execute a command and may support contextual arguments as specified by the constructor {@link Command#Command(Argument...)}.
 * 
 * To integrate your {@link Command} into an {@link Argument} use {@link ArgumentFactory#command(Command)}
 * or {@link CommandLineParser#withCommands(Command...)} if you have several commands.
 * If you support several commands and a user enters several of them at the same
 * time they will be executed in the order given to {@link CommandLineParser#parse(String...)}.
 * 
 * <b>Mutability note:</b> although a {@link Command} should be {@link Immutable}
 * the objects it handles doesn't have to be. So repeated invocations of execute
 * is allowed to yield different results or to affect external state.
 * </pre>
 * 
 * Example subclass:
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * public class BuildCommand extends Command
 * {
 *   private static final Argument&lt;File&gt; PATH = fileArgument().description("the directory to build").build();
 * 
 *   public BuildCommand()
 *   {
 *     super(PATH);
 *   }
 * 
 *   public String commandName()
 *   {
 *     return "build";
 *   }
 * 
 *   public String description()
 *   {
 *     return "Builds a target";
 *   }
 * 
 *   protected void execute(ParsedArguments parsedArguments)
 *   {
 *     File pathToBuild = parsedArguments.get(PATH);
 *     //Build pathToBuild here
 *   }
 * }
 * </code>
 * </pre>
 * 
 * And the glue needed to integrate the BuildCommand with a {@link CommandLineParser}:
 * 
 * <pre class="prettyprint">
 * <code class="language-java">
 * CommandLineParser.withCommands(new BuildCommand()).parse("build", "some_directory_that_needs_building");
 * </code>
 * </pre>
 */
@Immutable
public abstract class Command extends InternalStringParser<String> implements Description
{
	@Nonnull private final ImmutableList<Argument<?>> commandArguments;

	/**
	 * @param commandArguments the arguments that this command supports.
	 */
	protected Command(Argument<?> ... commandArguments)
	{
		this.commandArguments = ImmutableList.copyOf(commandArguments);
	}

	/**
	 * The name that triggers this command. For several names override this with
	 * {@link ArgumentBuilder#names(String...)}
	 */
	@Nonnull
	@CheckReturnValue
	protected abstract String commandName();

	/**
	 * Called when this command is encountered on the command line
	 * 
	 * @param parsedArguments a container with parsed values for the (optional) command arguments,
	 *            as specified by {@link Command#Command(Argument...)}
	 */
	protected abstract void execute(ParsedArguments parsedArguments);

	/**
	 * Override to provide a description to print in the usage text for this command.
	 * This is essentially an alternative to {@link ArgumentBuilder#description(Description)}
	 * 
	 * @return the description to use in the usage text
	 */
	@Override
	@Nonnull
	public String description()
	{
		return "";
	}

	@Override
	public String toString()
	{
		return commandName();
	}

	@Override
	final String parse(final ArgumentIterator arguments, final String previousOccurance, final ArgumentSettings argumentSettings, Locale locale)
			throws ArgumentException
	{
		String usedCommandName = arguments.current();
		arguments.rememberAsCommand();
		ParsedArguments parsedArguments = parser().parse(arguments, locale);
		execute(parsedArguments);
		return usedCommandName;
	}

	/**
	 * The parser for parsing the {@link Argument}s passed to {@link Command#Command(Argument...)}
	 */
	private CommandLineParserInstance parser()
	{
		return commandArgumentParser.get();
	}

	@Nonnull private final Supplier<CommandLineParserInstance> commandArgumentParser = Suppliers.memoize(new Supplier<CommandLineParserInstance>(){
		@Override
		public CommandLineParserInstance get()
		{
			return CommandLineParserInstance.createCommandParser(commandArguments);
		}
	});

	@Override
	final String defaultValue()
	{
		return null;
	}

	@Override
	final String descriptionOfValidValues(ArgumentSettings argumentSettings, Locale locale)
	{
		return parser().commandUsage(locale);
	}

	@Override
	final String describeValue(String value)
	{
		return value;
	}

	@Override
	final String metaDescription(ArgumentSettings argumentSettings)
	{
		return "";
	}
}
