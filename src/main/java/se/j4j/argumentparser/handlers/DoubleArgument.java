package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentHandler;

public class DoubleArgument implements ArgumentHandler<Double>
{
	public Double parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return Double.valueOf(currentArgument.next());
	}
}
