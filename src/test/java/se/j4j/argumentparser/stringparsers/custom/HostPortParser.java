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
		return new HostPort("localhost", 8080);
	}

	@Override
	public HostPort parse(String argument) throws ArgumentException
	{
		String[] s = argument.split(":");
		return new HostPort(s[0], Integer.parseInt(s[1]));
	}

}
