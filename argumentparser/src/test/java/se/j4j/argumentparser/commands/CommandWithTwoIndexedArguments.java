package se.j4j.argumentparser.commands;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.Arrays;
import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.ParsedArguments;

public class CommandWithTwoIndexedArguments extends Command
{
	private static final Argument<List<Integer>> NUMBERS = integerArgument().arity(2).required().build();

	public CommandWithTwoIndexedArguments()
	{
		super(NUMBERS);
	}

	@Override
	protected String commandName()
	{
		return "two_args";
	}

	@Override
	protected void execute(ParsedArguments args)
	{
		assertThat(args.get(NUMBERS)).isEqualTo(Arrays.asList(1, 2));
	}
}
