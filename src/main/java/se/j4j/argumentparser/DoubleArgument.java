package se.j4j.argumentparser;

import java.util.ListIterator;

public class DoubleArgument extends Argument<Double>
{
	DoubleArgument(final String ... names)
	{
		super(names);
	}

	@Override
	Double parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Double.valueOf(currentArgument.next());
	}

}
