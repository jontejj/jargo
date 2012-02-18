package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class IntegerArgument extends RadixiableArgument<Integer>
{
	/**
	 * @Throws {@link NumberFormatException} if <code> currentArgument.next() </code> does not contain a parsable integer.
	 */
	@Override
	public Integer parse(final String value) throws ArgumentException
	{
		try
		{
			return Integer.parseInt(value, radix());
		}
		catch(NumberFormatException ex)
		{
			throw InvalidArgument.create(value, " is not a valid integer of radix " + radix());
		}
	}
}
