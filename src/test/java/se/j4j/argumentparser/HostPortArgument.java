package se.j4j.argumentparser;

import se.j4j.argumentparser.handlers.OneParameterArgument;

public class HostPortArgument extends OneParameterArgument<HostPort>
{
	@Override
	public HostPort parse(final String value)
	{
		HostPort result = new HostPort();
		String[] s = value.split(":");
		result.host = s[0];
		result.port = Integer.parseInt(s[1]);
		return result;
	}

}
