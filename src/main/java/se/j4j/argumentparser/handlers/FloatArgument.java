package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.InvalidArgument;

public class FloatArgument implements ArgumentHandler<Float>
{
	public Float parse(final ListIterator<String> currentArgument) throws InvalidArgument
	{
		String value = currentArgument.next();
		try
		{
			return Float.valueOf(currentArgument.next());
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid float value");
		}
	}
}
