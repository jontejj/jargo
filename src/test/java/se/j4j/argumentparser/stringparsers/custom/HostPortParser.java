package se.j4j.argumentparser.stringparsers.custom;

import se.j4j.argumentparser.ArgumentException;
import se.j4j.argumentparser.StringParser;

public class HostPortParser implements StringParser<HostPort>
{
	@Override
	public String descriptionOfValidValues()
	{
		return "port:host";
	}

	@Override
	public HostPort defaultValue()
	{
		return HostPort.DEFAULT;
	}

	@Override
	public HostPort parse(String argument) throws ArgumentException
	{
		return HostPort.parse(argument);
	}

	@Override
	public String metaDescription()
	{
		return "<hostinformation>";
	}

}
