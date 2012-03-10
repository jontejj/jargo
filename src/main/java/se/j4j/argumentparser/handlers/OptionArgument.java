package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.builders.Argument;
import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.interfaces.ArgumentHandler;

public class OptionArgument implements ArgumentHandler<Boolean>
{
	final Boolean defaultValue;
	public OptionArgument(final boolean defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	public Boolean parse(final ListIterator<String> currentArgument, final Boolean oldValue, final Argument<?> argumentDefinition) throws ArgumentException
	{
		return !defaultValue;
	}

	public String descriptionOfValidValues()
	{
		return "not needed";
	}

}
