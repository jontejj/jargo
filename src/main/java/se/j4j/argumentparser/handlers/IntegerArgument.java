package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.InvalidArgument;

public class IntegerArgument extends RadixiableArgument<Integer>
{
	/**
	 * @Throws {@link NumberFormatException} if <code> currentArgument.next() </code> does not contain a parsable integer.
	 */
	public Integer parse(final ListIterator<String> currentArgument) throws InvalidArgument
	{
		String value = currentArgument.next();
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
