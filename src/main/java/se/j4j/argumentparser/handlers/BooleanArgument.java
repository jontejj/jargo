package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentHandler;

/**
 * Returns a Boolean with a value represented by the next string in the arguments.
 * The Boolean returned represents a true value if the string argument is equal, ignoring case, to the string "true".
 */
public class BooleanArgument implements ArgumentHandler<Boolean>
{
	public Boolean parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return currentArgument.next().equalsIgnoreCase("true");
	}
}
