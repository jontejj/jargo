package se.j4j.argumentparser;

import java.util.ListIterator;

public class ByteArgument extends RadixiableArgument<Byte>
{

	ByteArgument(final String[] names)
	{
		super(names);
	}

	@Override
	Byte parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Byte.valueOf(currentArgument.next(), radix());
	}

}
