package se.j4j.argumentparser;

import java.io.Serializable;
import java.util.Collection;

public class MissingRequiredArgumentException extends ArgumentException
{
	private Collection<Argument<?>> missingArguments;

	private MissingRequiredArgumentException(final Collection<Argument<?>> missingArguments)
	{
		super(ArgumentExceptionCodes.MISSING_REQUIRED_PARAMETER);
		this.missingArguments = missingArguments;
	}

	public static MissingRequiredArgumentException create(final Collection<Argument<?>> missingArguments)
	{
		return new MissingRequiredArgumentException(missingArguments);
	}

	@Override
	public String getMessage()
	{
		return super.getMessage() + ". Missing arguments: " + missingArguments;
	}

	Collection<Argument<?>> missingArguments()
	{
		return missingArguments;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long	serialVersionUID	= 1L;
}
