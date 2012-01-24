package se.j4j.argumentparser;

import java.util.ListIterator;

/**
 * Returns a Boolean with a value represented by the next string in the arguments.
 * The Boolean returned represents a true value if the string argument is equal, ignoring case, to the string "true".
 */
public class BooleanArgument extends Argument<Boolean>
{
	protected BooleanArgument(final String ...names)
	{
		super(names);
	}

	@Override
	Boolean parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		return currentArgument.next().equalsIgnoreCase("true");
	}
}
