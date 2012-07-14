package se.j4j.argumentparser;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParser.Arguments;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.InternalStringParser;

/**
 * <pre>
 * {@link Command}s are used for advanced {@link Argument}s that have a {@link CommandLineParser} themselves.
 * That is they execute a command and may support contextual arguments.
 * 
 * To integrate your {@link Command} into an {@link Argument} use {@link ArgumentFactory#command(Command)}
 * or {@link CommandLineParser#forCommands(Command...)} if you have several commands.
 * 
 * <b>Mutability note:</b> although a {@link Command}
 * should be {@link Immutable} the objects it handles doesn't have to be.
 * So repeated invocations of execute is allowed to yield different results
 * or to affect external state.
 * 
 * TODO: show code example
 * </pre>
 */
@Immutable
public abstract class Command extends InternalStringParser<String>
{
	/**
	 * Will only be called once and only if this command is encountered.
	 * TODO: should this have a default implementation calling
	 * {@link CommandLineParser#forAnyArguments()}?
	 * 
	 * @return a {@link CommandLineParser} for the arguments this command supports,
	 *         use {@link CommandLineParser#forAnyArguments()} if this command doesn't need any
	 *         extra arguments.
	 */
	@CheckReturnValue
	@Nonnull
	protected abstract CommandLineParser createParserForCommandArguments();

	/**
	 * At least one name should be used to trigger this Command.
	 * For several names (or none) override this with {@link ArgumentBuilder#names(String...)}
	 * 
	 * @return the default name that this command uses
	 */
	@Nonnull
	@CheckReturnValue
	public abstract String commandName();

	/**
	 * Called when this command should be executed.
	 * May be executed from different threads with different arguments
	 * 
	 * @param parsedArguments
	 */
	protected abstract void execute(@Nonnull ParsedArguments parsedArguments);

	private final Cache<CommandLineParser> subArgumentParser = new Cache<CommandLineParser>(){
		@Override
		protected CommandLineParser createInstance()
		{
			return createParserForCommandArguments();
		}
	};

	private CommandLineParser parser()
	{
		return subArgumentParser.getCachedInstance();
	}

	@Override
	final String parse(final Arguments arguments, final String previousOccurance, final ArgumentSettings argumentSettings) throws ArgumentException
	{
		// TODO: test running CommitCommand & InitCommand in the same parse,
		// arguments probably needs to be copied and not just passed on
		ParsedArguments result = parser().parse(arguments);
		execute(result);
		return commandName(); // Can be used to check for the existence of this
								// command in the given input arguments
	}

	@Override
	public String toString()
	{
		return commandName();
	}

	// TODO: provide usage and validValues

	@Override
	public String defaultValue()
	{
		return null;
	}

	@Override
	String descriptionOfValidValues(ArgumentSettings argumentSettings)
	{
		return parser().usage("");
	}

	@Override
	String describeValue(String value, ArgumentSettings argumentSettings)
	{
		return value;
	}

	@Override
	String metaDescription(ArgumentSettings argumentSettings)
	{
		// TODO: verify that this looks ok
		return "";
	}
}
