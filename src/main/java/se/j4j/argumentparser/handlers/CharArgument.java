package se.j4j.argumentparser.handlers;

import se.j4j.argumentparser.exceptions.ArgumentException;
import se.j4j.argumentparser.exceptions.InvalidArgument;

public class CharArgument extends OneParameterArgument<Character>
{
	@Override
	public Character parse(final String value) throws ArgumentException
	{
		if(value.length() != 1)
			throw InvalidArgument.create(value, " is not a valid character");

		return value.charAt(0);
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "Any unicode character";
	}

	@Override
	public Character defaultValue()
	{
		return 0;
	}
}
