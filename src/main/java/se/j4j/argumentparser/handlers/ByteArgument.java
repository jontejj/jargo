package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class ByteArgument extends RadixiableArgument<Byte>
{
	@Override
	public Byte parse(final String value) throws ArgumentException
	{
		try
		{
			return Byte.valueOf(value, radix());
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid byte of radix " + radix());
		}
	}
}
