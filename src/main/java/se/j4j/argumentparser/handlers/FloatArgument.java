package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class FloatArgument extends OneParameterArgument<Float>
{
	@Override
	public Float parse(final String value) throws ArgumentException
	{
		try
		{
			return Float.valueOf(value);
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid float value");
		}
	}

	@Override
	public String descriptionOfValidValues()
	{
		return -Float.MAX_VALUE + " - " + Float.MAX_VALUE;
	}

	@Override
	public Float defaultValue()
	{
		return 0f;
	}
}
