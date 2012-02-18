package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class ShortArgument extends RadixiableArgument<Short>
{
	@Override
	public Short parse(final String value) throws ArgumentException
	{
		try
		{
			return Short.valueOf(value, radix());
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid short value");
		}
	}
}
