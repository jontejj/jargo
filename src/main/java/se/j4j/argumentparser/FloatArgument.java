package se.j4j.argumentparser;

import java.util.ListIterator;

public class FloatArgument extends Argument<Float>
{
	FloatArgument(final String ... names)
	{
		super(names);
	}

	@Override
	Float parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Float.valueOf(currentArgument.next());
	}

}
