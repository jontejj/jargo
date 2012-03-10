package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class DoubleArgument extends OneParameterArgument<Double>
{
	@Override
	public Double parse(final String value) throws ArgumentException
	{
		try
		{
			return Double.valueOf(value);
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid double");
		}
	}

	public String descriptionOfValidValues()
	{
		return -Double.MAX_VALUE + " - " + Double.MAX_VALUE;
	}
}
