package se.j4j.argumentparser.internal;

/**
 * Manages code that needs to be platform specific so that all other code can be platform
 * independent.
 */
public final class Platform
{
	private Platform()
	{
	};

	/**
	 * A suitable string to represent newlines on this specific platform
	 */
	public static final String NEWLINE = System.lineSeparator();
}
