package se.j4j.argumentparser.handlers;

public class StringArgument extends OneParameterArgument<String>
{
	@Override
	public String parse(final String value)
	{
		return value;
	}
}
