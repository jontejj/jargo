package se.j4j.argumentparser;

import java.util.ListIterator;

public class CharArgument extends Argument<Character>
{
	CharArgument(final String ... names)
	{
		super(names);
	}

	@Override
	Character parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		String value = currentArgument.next();
		if(value.length() != 1)
		{
			throw InvalidArgument.create(this, value);
		}
		return value.charAt(0);
	}

}
