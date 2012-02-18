package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.builders.Argument;

public class OptionArgument implements ArgumentHandler<Boolean>
{
	final Boolean defaultValue;
	public OptionArgument(final boolean defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public Boolean parse(final ListIterator<String> currentArgument, final Argument<?> argumentDefinition)
	{
		return !defaultValue;
	}
}
