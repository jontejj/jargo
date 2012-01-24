package se.j4j.argumentparser;

import java.util.ListIterator;

public class LongArgument extends RadixiableArgument<Long>
{

	@Override
	Long parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Long.valueOf(currentArgument.next(), radix());
	}

}
