package se.j4j.argumentparser;

import java.io.Serializable;

public class InvalidArgument extends ArgumentException
{
	private Argument<?> argumentHandler;
	private String invalidValue;

	protected InvalidArgument(final Argument<?> argumentHandler, final String invalidValue)
	{
		super(ArgumentExceptionCodes.INVALID_PARAMTER);
		this.argumentHandler = argumentHandler;
		this.invalidValue = invalidValue;
	}


	public static InvalidArgument create(final Argument<?> argumentHandler, final String invalidValue)
	{
		return new InvalidArgument(argumentHandler, invalidValue);
	}


	@Override
	public String getMessage()
	{
		return super.getMessage() + ". Invalid value: " + invalidValue + " for argument: " + argumentHandler;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long	serialVersionUID	= 1L;
}
