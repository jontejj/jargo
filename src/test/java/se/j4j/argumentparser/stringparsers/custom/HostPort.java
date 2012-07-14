package se.j4j.argumentparser.stringparsers.custom;

public class HostPort
{
	public final int port;
	public final String host;

	private HostPort(final String host, final int port)
	{
		this.port = port;
		this.host = host;
	}

	@Override
	public String toString()
	{
		return host + ":" + port;
	}

	public static HostPort parse(String hostAndPort)
	{
		String[] s = hostAndPort.split(":");
		return new HostPort(s[0], Integer.parseInt(s[1]));
	}

	public static final HostPort DEFAULT = new HostPort("localhost", 8080);
}
