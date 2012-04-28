package se.j4j.argumentparser.handlers;

public class StringArgument extends OneParameterArgument<String>
{
	@Override
	public String parse(final String value)
	{
		return value;
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "Any string";
	}

	@Override
	public String defaultValue()
	{
		return "";
	}
}
