package se.j4j.argumentparser.stringparsers.custom;

import java.util.Locale;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class PortParser implements StringParser<Port>
{
	@Override
	public String descriptionOfValidValues(Locale locale)
	{
		return "a port number between " + Port.MIN + " and " + Port.MAX;
	}

	@Override
	public Port defaultValue()
	{
		return Port.DEFAULT;
	}

	@Override
	public Port parse(String argument, Locale locale) throws ArgumentException
	{
		return Port.parse(argument);
	}

	@Override
	public String metaDescription()
	{
		return "<port number>";
	}

}
