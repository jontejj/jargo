package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentHandler;

public class StringArgument implements ArgumentHandler<String>
{
	public String parse(final ListIterator<String> currentArgument)
	{
		return currentArgument.next();
	}
}
