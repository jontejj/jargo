package se.j4j.argumentparser;

import java.util.ListIterator;

public class StringArgument extends Argument<String>
{
	protected StringArgument(final String ...names)
	{
		super(names);
	}

	@Override
	String parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return currentArgument.next();
	}
}
