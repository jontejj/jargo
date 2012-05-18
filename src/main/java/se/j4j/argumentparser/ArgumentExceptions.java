package se.j4j.argumentparser;

import static com.google.common.collect.Collections2.transform;
import static se.j4j.argumentparser.Describers.argumentDescriber;
import static se.j4j.argumentparser.Describers.functionFor;

import java.io.Serializable;
import java.util.Collection;
import java.util.ListIterator;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ArgumentExceptions
{
	private ArgumentExceptions()
	{
	}

	public static ArgumentException forErrorCode(final ArgumentExceptionCodes errorCode)
	{
		return new ArgumentException(errorCode);
	}

	public static InvalidArgument forInvalidValue(final Object invalidValue, final String explanation)
	{
		return new InvalidArgument(Descriptions.forString(explanation), invalidValue);
	}

	public static InvalidArgument forInvalidValue(final Object invalidValue, final Description explanation)
	{
		return new InvalidArgument(explanation, invalidValue);
	}

	public static MissingRequiredArgumentException forMissingArguments(final Collection<Argument<?>> missingArguments, final CommandLineParser parser)
	{
		MissingRequiredArgumentException exception = new MissingRequiredArgumentException(missingArguments);
		exception.setOriginParser(parser);
		return exception;
	}

	@Nonnull
	public static LimitException forLimit(@Nonnull Limit reason)
	{
		return new LimitException(reason);
	}

	/**
	 * @param exceptionMessage the {@link Description} to use in {@link Throwable#getMessage()}
	 * @return an unchecked exception that uses {@link Description#description()} as
	 *         {@link Throwable#getMessage()}
	 */
	@CheckReturnValue
	@Nonnull
	public static IllegalArgumentException withDescription(@Nonnull final Description exceptionMessage, @Nullable final Throwable cause)
	{
		return new UncheckedArgumentException(exceptionMessage, cause);
	}

	@CheckReturnValue
	static <T> UnhandledRepeatedArgument forUnhandledRepeatedArgument(final Argument<T> unhandledArgument, final T oldValue)
	{
		// TODO: handle indexed arguments as well,
		// TODO: verify that the actual value from the commandline is used
		// (finalizers should not have changed the value for example),
		// think about arity, split with when printing the previous value
		return new UnhandledRepeatedArgument(unhandledArgument.names() + ", previous value: " + unhandledArgument.parser().describeValue(oldValue));
	}

	@CheckReturnValue
	static UnhandledRepeatedArgument forUnhandledRepeatedArgument(final String reason)
	{
		return new UnhandledRepeatedArgument(reason);
	}

	@Nonnull
	static UnexpectedArgumentException forUnexpectedArgument(@Nonnull final ListIterator<String> arguments)
	{
		String unexpectedArgument = arguments.previous();
		String previousArgument = null;
		if(arguments.hasPrevious())
		{
			previousArgument = arguments.previous();
		}

		return new UnexpectedArgumentException(unexpectedArgument, previousArgument);
	}

	public static final class InvalidArgument extends ArgumentException
	{
		private final Description explanation;
		private final Object invalidValue;

		private InvalidArgument(final Description explanation, final Object invalidValue)
		{
			super(ArgumentExceptionCodes.INVALID_PARAMETER);
			this.explanation = explanation;
			this.invalidValue = invalidValue;
		}

		@Override
		public String getMessage()
		{
			return "'" + invalidValue + "'" + explanation.description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	public static final class MissingRequiredArgumentException extends ArgumentException
	{
		private final Collection<Argument<?>> missingArguments;

		private MissingRequiredArgumentException(final Collection<Argument<?>> missingArguments)
		{
			super(ArgumentExceptionCodes.MISSING_REQUIRED_PARAMETER);
			this.missingArguments = missingArguments;
		}

		@Override
		public String getMessage()
		{
			return "Missing required arguments: " + transform(missingArguments, functionFor(argumentDescriber()));
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	public static final class LimitException extends ArgumentException
	{
		private final Limit reason;

		private LimitException(Limit reason)
		{
			super(ArgumentExceptionCodes.PARAMETER_VALUE_NOT_WITHIN_LIMITS);
			this.reason = reason;
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

	public static final class UnexpectedArgumentException extends ArgumentException
	{
		private final String unexpectedArgument;
		private final String previousArgument;

		private UnexpectedArgumentException(@Nonnull final String unexpectedArgument, @Nullable final String previousArgument)
		{
			super(ArgumentExceptionCodes.UNHANDLED_PARAMETER);
			this.unexpectedArgument = unexpectedArgument;
			this.previousArgument = previousArgument;
		}

		@Override
		public String getMessage()
		{
			String message = "Unexpected argument: " + unexpectedArgument;
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

	public static final class UnhandledRepeatedArgument extends ArgumentException
	{
		private final Object reason;

		private UnhandledRepeatedArgument(final Object reason)
		{
			super(ArgumentExceptionCodes.UNHANDLED_REPEATED_PARAMETER);
			this.reason = reason;
		}

		@Override
		public String getMessage()
		{
			// TODO: verify for propertyMaps
			return "Non-allowed repetition of: " + reason;
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	private static final class UncheckedArgumentException extends IllegalArgumentException
	{
		private final Description message;

		UncheckedArgumentException(final Description message, final Throwable cause)
		{
			super(cause);
			this.message = message;
		}

		@Override
		public String getMessage()
		{
			return message.description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	};

	public enum ArgumentExceptionCodes
	{
		// TODO: these should take in an argument describing the exact cause

		/**
		 * Used when
		 * "-p 8080" is expected but
		 * "-p" is given
		 */
		MISSING_PARAMETER,

		/**
		 * Used when
		 * "1 2" is expected but
		 * "1 2 3" is given
		 */
		UNHANDLED_PARAMETER,

		/**
		 * Thrown when {@link Argument#required()} has been specified but the
		 * argument wasn't found in the input arguments
		 */
		MISSING_REQUIRED_PARAMETER,

		/**
		 * Used when
		 * "--numbers 1 2" is expected but
		 * "--numbers 1 2 --numbers 3 4" is given
		 */
		UNHANDLED_REPEATED_PARAMETER,

		/**
		 * May be thrown by {@link StringParser#parse(String)} when
		 * it considers it's received argument to be invalid
		 */
		INVALID_PARAMETER,

		PARAMETER_VALUE_NOT_WITHIN_LIMITS,

		UNKNOWN
	}
}
