package se.j4j.argumentparser.commands;

import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

final class ProfilingCommand extends Command
{
	public ProfilingCommand()
	{
		// Two arguments with the same name ("-n" here) should trigger an exception when building a
		// command line parser, but as that's only done when the command is encountered it shouldn't
		// trigger here
		super(integerArgument("-n").build(), integerArgument("-n").build());
	}

	@Override
	public String commandName()
	{
		return "profile";
	}

	@Override
	protected void execute(ParsedArguments parsedArguments)
	{
	}
}
