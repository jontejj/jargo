package se.j4j.argumentparser.exceptions;

import java.io.Serializable;

public class UnexpectedArgumentException extends ArgumentException
{

	final String unexpectedArgument;

	private UnexpectedArgumentException(final String unexpectedArgument)
	{
		super(ArgumentExceptionCodes.UNHANDLED_PARAMETER);
		this.unexpectedArgument = unexpectedArgument;
	}

	public static UnexpectedArgumentException unexpectedArgument(final String unexpectedArgument)
	{
		return new UnexpectedArgumentException(unexpectedArgument);
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ". Unexpected argument: " + unexpectedArgument;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long	serialVersionUID	= 1L;
}
