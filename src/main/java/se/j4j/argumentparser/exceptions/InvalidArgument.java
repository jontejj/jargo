package se.j4j.argumentparser.exceptions;

import java.io.Serializable;

public class InvalidArgument extends ArgumentException
{
	private final String explanation;
	private final String invalidValue;

	protected InvalidArgument(final String explanation, final String invalidValue)
	{
		super(ArgumentExceptionCodes.INVALID_PARAMTER);
		this.explanation = explanation;
		this.invalidValue = invalidValue;
	}

	public static InvalidArgument create(final Object invalidValue, final String explanation)
	{
		return new InvalidArgument(explanation, String.valueOf(invalidValue));
	}

	@Override
	public String getMessage()
	{
		return "'" + invalidValue + "'" + explanation;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
