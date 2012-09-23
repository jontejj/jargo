package se.j4j.argumentparser;

import static java.util.Collections.emptyList;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.InternalStringParser;
import se.j4j.strings.Description;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * <pre>
 * {@link Command}s automatically gets an invocation of {@link #execute(ParsedArguments)} when given on the command line.
 * This is particularly useful to avoid a never ending switch statement.
 * 
 * {@link Command}s have a {@link CommandLineParser} themselves, that is,
 * they execute a command and may support contextual arguments as specified by {@link #commandArguments()}.
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
 *   protected List&lt;Argument&lt;?&gt;&gt; commandArguments()
 *   {
 *     List&lt;Argument&lt;?&gt;&gt; arguments = Lists.newArrayList();
 *     arguments.add(PATH);
 *     return arguments;
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
	 * @param parsedArguments a container with parsed values for the {@link #commandArguments()}
	 */
	protected abstract void execute(ParsedArguments parsedArguments);

	/**
	 * <pre>
	 * The arguments that is specific for this command.
	 * 
	 * Will only be called once and only if this command is encountered.
	 * 
	 * @return a list of arguments that this command supports
	 * 
	 * TODO: what can List<Argument<?>> be replaced with so that callers can use Arrays.asList(someArgument);?
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	protected List<Argument<?>> commandArguments()
	{
		return emptyList();
	}

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
	final String parse(final ArgumentIterator arguments, final String previousOccurance, final ArgumentSettings argumentSettings)
			throws ArgumentException
	{
		ParsedArguments commandArguments = parser().parse(arguments);
		execute(commandArguments);
		return commandName(); // Can be used to check for the existence of this
								// command in the given input arguments
	}

	@Nonnull private final Supplier<CommandLineParser> commandArgumentParser = Suppliers.memoize(new Supplier<CommandLineParser>(){
		@Override
		public CommandLineParser get()
		{
			return CommandLineParser.createCommandParser(commandArguments());
		}
	});

	private CommandLineParser parser()
	{
		return commandArgumentParser.get();
	}

	@Override
	final String defaultValue()
	{
		return null;
	}

	@Override
	final String descriptionOfValidValues(ArgumentSettings argumentSettings)
	{
		return parser().usage("");
	}

	@Override
	final String describeValue(String value, ArgumentSettings argumentSettings)
	{
		return value;
	}

	@Override
	final String metaDescription(ArgumentSettings argumentSettings)
	{
		return "";
	}
}
