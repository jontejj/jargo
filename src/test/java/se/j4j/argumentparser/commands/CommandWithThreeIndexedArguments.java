package se.j4j.argumentparser.commands;

import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.ArgumentFactory.integerArgument;

import java.util.Arrays;
import java.util.List;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.Command;
import se.j4j.argumentparser.CommandLineParser.ParsedArguments;

public class CommandWithThreeIndexedArguments extends Command
{
	private static final Argument<List<Integer>> NUMBERS = integerArgument().arity(3).required().build();

	@Override
	protected List<Argument<?>> commandArguments()
	{
		return Arrays.<Argument<?>>asList(NUMBERS);
	}

	@Override
	protected String commandName()
	{
		return "three_args";
	}

	@Override
	protected void execute(ParsedArguments args)
	{
		assertThat(args.get(NUMBERS)).isEqualTo(Arrays.asList(1, 2, 3));
	}
}
