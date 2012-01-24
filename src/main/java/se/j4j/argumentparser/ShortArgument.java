package se.j4j.argumentparser;

import java.util.ListIterator;

public class ShortArgument extends RadixiableArgument<Short>
{
	ShortArgument(final String ... names)
	{
		super(names);
	}

	@Override
	Short parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Short.valueOf(currentArgument.next(), radix());
	}

}
