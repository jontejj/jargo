package se.j4j.argumentparser;

import java.io.Serializable;


public class ArgumentException extends Exception
{
	private final ArgumentExceptionCodes errorCode;
	private Argument<?> errorneousArgument;
	protected ArgumentException(final ArgumentExceptionCodes errorCode)
	{
		this.errorCode = errorCode;
	}

	public ArgumentException errorneousArgument(final Argument<?> errorneousArgument)
	{
		this.errorneousArgument = errorneousArgument;
		return this;
	}


	public static ArgumentException create(final ArgumentExceptionCodes errorCode)
	{
		return new ArgumentException(errorCode);
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ": Error code: " + errorCode;
	}

	@Override
	public ArgumentException initCause(final Throwable cause)
	{
		super.initCause(cause);
		return this;
	}

	public Argument<?> errorneousArgument()
	{
		return errorneousArgument;
	}

	public ArgumentExceptionCodes code()
	{
		return errorCode;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long	serialVersionUID	= 1L;

}
