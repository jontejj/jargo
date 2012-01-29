package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.InvalidArgument;

public class ShortArgument extends RadixiableArgument<Short>
{
	public Short parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		String value = currentArgument.next();
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
