package se.j4j.argumentparser.handlers;


/**
 * Returns a Boolean with a value represented by the next string in the arguments.
 * The Boolean returned represents a true value if the string argument is equal, ignoring case, to the string "true".
 */
public class BooleanArgument extends OneParameterArgument<Boolean>
{
	@Override
	public Boolean parse(final String value)
	{
		return Boolean.valueOf(value);
	}

	public String descriptionOfValidValues()
	{
		return "true or false";
	}
}
