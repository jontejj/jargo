package se.j4j.argumentparser;

import java.io.Serializable;

public class UnhandledRepeatedArgument extends ArgumentException
{
	private Argument<?> unhandledArgument;

	private UnhandledRepeatedArgument(final Argument<?> unhandledArgument)
	{
		super(ArgumentExceptionCodes.UNHANDLED_REPEATED_PARAMETER);
		this.unhandledArgument = unhandledArgument;
	}

	public static UnhandledRepeatedArgument create(final Argument<?> unhandledArgument)
	{
		return new UnhandledRepeatedArgument(unhandledArgument);
	}


	@Override
	public String getMessage()
	{
		return super.getMessage() + ". Non-allowed repetion of argument: " + unhandledArgument;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long	serialVersionUID	= 1L;
}
