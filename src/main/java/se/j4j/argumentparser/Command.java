package se.j4j.argumentparser;

import java.util.ListIterator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.CommandLineParsers.ParsedArguments;
import se.j4j.argumentparser.StringParsers.InternalStringParser;

/**
 * <pre>
 * {@link Command}s are used for advanced {@link Argument}s that have a {@link CommandLineParser} themselves.
 * That is they execute a command and may support additional arguments.
 * 
 * To integrate your {@link Command} into an {@link Argument} use {@link ArgumentFactory#command(Command)}.
 * 
 * <b>Mutability note:</b> although a {@link Command}
 * should be {@link Immutable} the objects it handles doesn't have to be.
 * So repeated invocations of {@link #execute(ParsedArguments)} is allowed to yield different results
 * or to affect external state.
 * </pre>
 */
@Immutable
public abstract class Command extends InternalStringParser<String>
{
	/**
	 * Will only be called once and only if this command is encountered
	 * 
	 * @return a {@link CommandLineParser} for the arguments this command supports,
	 *         use {@link CommandLineParsers#forAnyArguments()} if this command doesn't need any
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
	final String parse(final ListIterator<String> currentArgument, final String previousOccurance, final Argument<?> argumentDefinition)
			throws ArgumentException
	{
		ParsedArguments result = parser().parse(currentArgument);
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
	public String descriptionOfValidValues()
	{
		return parser().usage("");
	}

	@Override
	public String defaultValue()
	{
		return null;
	}

	@Override
	String describeValue(String value)
	{
		return value;
	}
}
