package se.j4j.argumentparser.CustomHandlers;

import se.j4j.argumentparser.handlers.OneParameterArgument;

public class HostPortArgument extends OneParameterArgument<HostPort>
{
	@Override
	public HostPort parse(final String value)
	{
		String[] s = value.split(":");
		return new HostPort(s[0], Integer.parseInt(s[1]));
	}

	@Override
	public String descriptionOfValidValues()
	{
		return "port:host";
	}

}
