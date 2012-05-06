package se.j4j.argumentparser.CustomHandlers;

import se.j4j.argumentparser.StringConverter;
import se.j4j.argumentparser.exceptions.ArgumentException;

public class HostPortArgument implements StringConverter<HostPort>
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
	public HostPort convert(String argument) throws ArgumentException
	{
		String[] s = argument.split(":");
		return new HostPort(s[0], Integer.parseInt(s[1]));
	}

}
