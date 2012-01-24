package se.j4j.argumentparser;

import java.util.ListIterator;

public class HostPortArgument extends Argument<HostPort>
{
	@Override
	HostPort parse(final ListIterator<String> currentArgument) throws ArgumentException
	{
		HostPort result = new HostPort();
		String[] s = currentArgument.next().split(":");
		result.host = s[0];
		result.port = Integer.parseInt(s[1]);
		return result;
	}

}
