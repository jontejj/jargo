package se.j4j.argumentparser;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public class CommandParser extends Argument<String> implements ArgumentHandler<String>
{
	private static final String	UNUSED	= null;

	private final Argument<?>[] arguments;
	private final CommandExecutor commandExecutor;

	private ParsedArguments parsedArguments;

	CommandParser(final Argument<String> argument, final Argument<?>[] arguments, final CommandExecutor commandExecutor)
	{
		super(argument);
		this.arguments = arguments;
		this.commandExecutor = commandExecutor;
	}

	@Override
	public ArgumentHandler<?> handler()
	{
		return this;
	}

	public String parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		try
		{
			parsedArguments = ArgumentParser.forArguments(arguments).parse(currentArgument);
			if(commandExecutor != null)
			{
				commandExecutor.execute(parsedArguments);
			}
		}
		catch(ArgumentException ex)
		{
			if(commandExecutor != null)
			{
				commandExecutor.failed(ex);
			}
		}
		return UNUSED;
	}

	public <T> T get(final Argument<T> argumentToFetch)
	{
		return parsedArguments.get(argumentToFetch);
	}

}
