package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentException;

public class ByteArgument extends RadixiableArgument<Byte>
{
	public Byte parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Byte.valueOf(currentArgument.next(), radix());
	}
}
