package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.InvalidArgument;

public class LongArgument extends RadixiableArgument<Long>
{
	public Long parse(final ListIterator<String> currentArgument) throws InvalidArgument
	{
		String value = currentArgument.next();
		try
		{
			return Long.valueOf(value, radix());
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid long value");
		}
	}

}
