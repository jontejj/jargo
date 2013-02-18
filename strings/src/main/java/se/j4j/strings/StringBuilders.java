package se.j4j.strings;

public final class StringBuilders
{
	private StringBuilders()
	{
	}

	// TODO: write an API for reusing StringBuilders, a ThreadLocal<StringBuilder> perhaps? callers
	// must free the StringBuilder when they're done

	public static StringBuilder withExpectedSize(int expectedSize)
	{
		return new StringBuilder(expectedSize);
	}
}
