package se.j4j.argumentparser.handlers;

import java.util.ListIterator;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.ArgumentHandler;
import se.j4j.argumentparser.InvalidArgument;

public class CharArgument implements ArgumentHandler<Character>
{
	public Character parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		String value = currentArgument.next();
		if(value.length() != 1)
		{
			throw InvalidArgument.create(value, " is not a valid character");
		}
		return value.charAt(0);
	}

}
