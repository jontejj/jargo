package se.j4j.argumentparser.commands;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;
import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

public class CommandWithOneIndexedArgument extends Command
{
	private static final Argument<Integer> NUMBER = integerArgument().required().build();

	public CommandWithOneIndexedArgument()
	{
		super(NUMBER);
	}

	@Override
	protected String commandName()
	{
		return "one_arg";
	}

	@Override
	protected void execute(ParsedArguments args)
	{
		assertThat(args.get(NUMBER)).isEqualTo(1);
	}
}
