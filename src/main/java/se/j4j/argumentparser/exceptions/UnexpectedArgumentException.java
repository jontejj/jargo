package se.j4j.argumentparser.exceptions;

import java.io.Serializable;
import java.util.ListIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class UnexpectedArgumentException extends ArgumentException
{
	private final String unexpectedArgument;
	private final String previousArgument;

	private UnexpectedArgumentException(@Nonnull final String unexpectedArgument, @Nullable final String previousArgument)
	{
		super(ArgumentExceptionCodes.UNHANDLED_PARAMETER);
		this.unexpectedArgument = unexpectedArgument;
		this.previousArgument = previousArgument;
	}

	@Nonnull
	public static UnexpectedArgumentException unexpectedArgument(@Nonnull final ListIterator<String> arguments)
	{
		String unexpectedArgument = arguments.previous();
		String previousArgument = null;
		if(arguments.hasPrevious())
		{
			previousArgument = arguments.previous();
		}

		return new UnexpectedArgumentException(unexpectedArgument, previousArgument);
	}

	@Override
	public String getMessage()
	{
		String message = super.getMessage() + ". Unexpected argument: " + unexpectedArgument;
		if(previousArgument != null)
		{
			message += ", previous argument: " + previousArgument;
		}
		return message;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
