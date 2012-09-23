package se.j4j.argumentparser.stringparsers.custom;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class PortParser implements StringParser<Port>
{
	@Override
	public String descriptionOfValidValues()
	{
		return "a port number between 0 and " + Short.MAX_VALUE;
	}

	@Override
	public Port defaultValue()
	{
		return Port.DEFAULT;
	}

	@Override
	public Port parse(String argument) throws ArgumentException
	{
		return Port.parse(argument);
	}

	@Override
	public String metaDescription()
	{
		return "<port number>";
	}

}
