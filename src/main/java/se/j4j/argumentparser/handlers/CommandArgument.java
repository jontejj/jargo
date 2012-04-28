package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentBuilder;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.ArgumentParser.ParsedArguments;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

@Immutable
public abstract class CommandArgument implements ArgumentHandler<String>
{
	/**
	 * Will only be called once and only if this command is encountered
	 * 
	 * @return
	 */
	public abstract ArgumentParser createParserInstance();

	/**
	 * At least one name should be used to trigger this CommandArgument.
	 * For several names (or none) override this with
	 * {@link ArgumentBuilder#names(String...)}
	 * 
	 * @return the default name that this command uses
	 */
	@Nonnull
	@CheckReturnValue
	public abstract String commandName();

	/**
	 * May be executed from different threads with different arguments
	 * 
	 * @param parsedArguments
	 */
	public abstract void handle(ParsedArguments parsedArguments);

	@GuardedBy("this")// TODO: consider memory versus performance
	private volatile ArgumentParser parser;

	private void init()
	{
		synchronized(this)
		{
			if(parser == null)
			{
				parser = createParserInstance();
			}
		}
	};

	@Override
	public String parse(final ListIterator<String> currentArgument, final String handledBefore, final Argument<?> argumentDefinition)
			throws ArgumentException
	{
		init();
		ParsedArguments result = parser.parse(currentArgument);
		handle(result);
		return commandName(); // Can be used to check for the existence of this
								// argument in the given input arguments
	}

	@Override
	public String toString()
	{
		init();
		return parser.toString();
	}

	// TODO: provide usage and validValues

	@Override
	public String descriptionOfValidValues()
	{
		init();
		return parser.usage("").toString();
	}

	@Override
	public String defaultValue()
	{
		return null;
	}

	@Override
	public String describeValue(String value)
	{
		return value;
	}
}
