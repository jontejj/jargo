package se.j4j.argumentparser.exceptions;

import java.io.Serializable;

import se.j4j.argumentparser.Limit;

public final class LimitException extends ArgumentException
{
	private final Limit reason;

	private LimitException(Limit reason)
	{
		super(ArgumentExceptionCodes.PARAMETER_VALUE_NOT_WITHIN_LIMITS);
		this.reason = reason;
	}

	public static LimitException limitException(Limit reason)
	{
		return new LimitException(reason);
	}

	@Override
	public String getMessage()
	{
		return reason.reason();
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
