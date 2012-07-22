package se.j4j.argumentparser;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;
import se.j4j.argumentparser.StringParsers.InternalStringParser;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

/**
 * <pre>
 * {@link Command}s are used for advanced {@link Argument}s that have a {@link CommandLineParser} themselves.
 * That is they execute a command and may support contextual arguments.
 * 
 * To integrate your {@link Command} into an {@link Argument} use {@link ArgumentFactory#command(Command)}
 * or {@link CommandLineParser#forCommands(Command...)} if you have several commands. If you support several commands
 * and a user enters several of them at the same time they will be executed in the order given by the user.
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
	 * <pre>
	 * The arguments that is specific for this command.
	 * 
	 * Will only be called once and only if this command is encountered.
	 * 
	 * @return a list of arguments that this command supports
	 * </pre>
	 */
	@CheckReturnValue
	@Nonnull
	protected List<Argument<?>> commandArguments()
	{
		return ImmutableList.of();
	}

	/**
	 * At least one name should be used to trigger this Command.
	 * For several names override this with {@link ArgumentBuilder#names(String...)}
	 * 
	 * @return the default name that this command uses
	 */
	@Nonnull
	@CheckReturnValue
	public abstract String commandName();

	/**
	 * Called when this command should be executed.
	 * 
	 * @param parsedArguments a container with parsed values from the {@link #commandArguments()}
	 */
	protected abstract void execute(@Nonnull ParsedArguments parsedArguments);

	@Nonnull private final Supplier<CommandLineParser> commandArgumentParser = Suppliers.memoize(new Supplier<CommandLineParser>(){
		@Override
		public CommandLineParser get()
		{
			return new CommandLineParser(commandArguments(), true);
		}
	});

	private CommandLineParser parser()
	{
		return commandArgumentParser.get();
	}

	@Override
	final String parse(final ArgumentIterator arguments, final String previousOccurance, final ArgumentSettings argumentSettings)
			throws ArgumentException
	{
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

	@Override
	public final String defaultValue()
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
		return "";
	}
}
