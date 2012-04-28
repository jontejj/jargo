package se.j4j.argumentparser.exceptions;

import java.io.Serializable;

import javax.annotation.CheckReturnValue;

import se.j4j.argumentparser.Argument;

public class UnhandledRepeatedArgument extends ArgumentException
{
	private final Object reason;

	private UnhandledRepeatedArgument(final Object reason)
	{
		super(ArgumentExceptionCodes.UNHANDLED_REPEATED_PARAMETER);
		this.reason = reason;
	}

	@CheckReturnValue
	public static UnhandledRepeatedArgument create(final Argument<?> unhandledArgument)
	{
		return new UnhandledRepeatedArgument(unhandledArgument);
	}

	@CheckReturnValue
	public static UnhandledRepeatedArgument create(final String reason)
	{
		return new UnhandledRepeatedArgument(reason);
	}

	@Override
	public String getMessage()
	{
		// TODO: make this look pretty
		return super.getMessage() + ". Non-allowed repetition of: " + reason;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
