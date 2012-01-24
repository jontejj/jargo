package se.j4j.argumentparser;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentParser.ParsedArguments;

public class CommandParser extends Argument<String>
{
	private final String name;
	private Argument<?>[] arguments;
	private ParsedArguments parsedArguments;
	private CommandExecutor commandExecutor;

	CommandParser(final String ... names)
	{
		super(names);
		this.name = names[0];
	}

	public CommandParser setCommandExecutor(final CommandExecutor executor)
	{
		commandExecutor = executor;
		return this;
	}

	public CommandParser withArguments(final Argument<?>... arguments)
	{
		this.arguments = arguments;
		return this;
	}

	@Override
	String parse(final ListIterator<String> currentArgument) throws ArgumentException
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
		return name;
	}

	public <T> T get(final Argument<T> argumentToFetch)
	{
		return parsedArguments.get(argumentToFetch);
	}

}
