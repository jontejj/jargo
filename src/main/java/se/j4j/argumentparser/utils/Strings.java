package se.j4j.argumentparser.utils;

public final class Strings
{
	private Strings(){}

	public static StringBuilder appendSpaces(final int spacesToAppend, final StringBuilder toBuilder)
	{
		for(int i = 0; i < spacesToAppend; i++)
		{
			toBuilder.append(' ');
		}
		return toBuilder;
	}
}
