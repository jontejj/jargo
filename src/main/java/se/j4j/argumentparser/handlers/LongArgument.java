package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class LongArgument extends RadixiableArgument<Long>
{
	@Override
	public Long parse(final String value) throws ArgumentException
	{
		try
		{
			return Long.valueOf(value, radix());
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid long value");
		}
	}

	public String descriptionOfValidValues()
	{
		return Long.MIN_VALUE + " - " + Long.MAX_VALUE;
	}

}
