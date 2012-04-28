package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;

public class OptionArgument extends NoParameterArgument<Boolean>
{
	final Boolean defaultValue;

	public OptionArgument(final boolean defaultValue)
	{
		this.defaultValue = defaultValue;
	}

	@Override
	public Boolean defaultValue()
	{
		return defaultValue;
	}

	@Override
	public String describeValue(Boolean value)
	{
		return null;
	}

	@Override
	public Boolean get() throws ArgumentException
	{
		return !defaultValue;
	}
}
