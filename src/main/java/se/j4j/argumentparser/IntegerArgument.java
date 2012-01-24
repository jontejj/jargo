package se.j4j.argumentparser;

import java.util.ListIterator;

public class IntegerArgument extends RadixiableArgument<Integer>
{
	IntegerArgument(final String ...names)
	{
		super(names);
	}

	/**
	 * @Throws {@link NumberFormatException} if <code> currentArgument.next() </code> does not contain a parsable integer.
	 */
	@Override
	Integer parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Integer.parseInt(currentArgument.next(), radix());
	}
}
