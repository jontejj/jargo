package se.j4j.argumentparser.stringparsers.custom;

public class HostPort
{
	public final int port;
	public final String host;

	public HostPort(final String host, final int port)
	{
		this.port = port;
		this.host = host;
	}

	@Override
	public String toString()
	{
		return "localhost:8080";
	}
}
