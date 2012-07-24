package se.j4j.argumentparser;

import static com.google.common.collect.Collections2.transform;
import static se.j4j.argumentparser.Describers.argumentDescriber;
import static se.j4j.argumentparser.Describers.asFunction;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;

public final class ArgumentExceptions
{
	private ArgumentExceptions()
	{
	}

	public static InvalidArgument forInvalidValue(final Object invalidValue, final String explanation)
	{
		return new InvalidArgument(Descriptions.forString(explanation), invalidValue);
	}

	public static InvalidArgument forInvalidValue(final Object invalidValue, final Description explanation)
	{
		return new InvalidArgument(explanation, invalidValue);
	}

	/**
	 * @param exceptionMessage the {@link Description} to use in {@link Throwable#getMessage()}
	 * @return an unchecked exception that uses {@link Description#description()} as
	 *         {@link Throwable#getMessage()}
	 */
	@CheckReturnValue
	@Nonnull
	public static IllegalArgumentException withDescription(@Nonnull final Description exceptionMessage, @Nonnull final ArgumentException cause)
	{
		return new UncheckedArgumentException(exceptionMessage, cause);
	}

	static MissingRequiredArgumentException forMissingArguments(final Collection<Argument<?>> missingArguments, final CommandLineParser parser)
	{
		MissingRequiredArgumentException exception = new MissingRequiredArgumentException(missingArguments);
		exception.originatedFrom(parser);
		return exception;
	}

	@Nonnull
	static LimitException forLimit(@Nonnull Limit reason)
	{
		// TODO , see usage for '-i' for proper values.
		return new LimitException(reason);
	}

	@CheckReturnValue
	static <T> UnhandledRepeatedArgument forUnhandledRepeatedArgument(final Argument<T> unhandledArgument)
	{
		return new UnhandledRepeatedArgument("Non-allowed repetition of the argument " + unhandledArgument.names());
	}

	@CheckReturnValue
	static UnhandledRepeatedArgument forUnhandledRepeatedArgument(final String reason)
	{
		return new UnhandledRepeatedArgument(reason);
	}

	@CheckReturnValue
	static MissingParameterException forMissingParameter(ArgumentSettings argumentRequiringTheParameter, String usedArgumentName)
	{
		return new MissingParameterException(argumentRequiringTheParameter, usedArgumentName);
	}

	@Nonnull
	static UnexpectedArgumentException forUnexpectedArgument(@Nonnull final ArgumentIterator arguments)
	{
		String unexpectedArgument = arguments.previous();
		String previousArgument = null;
		if(arguments.hasPrevious())
		{
			previousArgument = arguments.previous();
		}

		return new UnexpectedArgumentException(unexpectedArgument, previousArgument);
	}

	/**
	 * May be thrown by {@link StringParser#parse(String)} when
	 * it considers it's received argument to be invalid
	 */
	public static final class InvalidArgument extends ArgumentException
	{
		private final Description explanation;
		private final Object invalidValue;

		private InvalidArgument(final Description explanation, final Object invalidValue)
		{
			this.explanation = explanation;
			this.invalidValue = invalidValue;
		}

		@Override
		public String getMessage()
		{
			return "'" + invalidValue + "' " + explanation.description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Used when
	 * "-p 8080" is expected but
	 * "-p" is given
	 */
	static final class MissingParameterException extends ArgumentException
	{
		private final String usedArgumentName;
		private final ArgumentSettings argumentRequiringTheParameter;

		private MissingParameterException(ArgumentSettings argumentRequiringTheParameter, final String usedArgumentName)
		{
			this.usedArgumentName = usedArgumentName;
			this.argumentRequiringTheParameter = argumentRequiringTheParameter;
		}

		@Override
		public String getMessage()
		{
			String parameterDescription = argumentRequiringTheParameter.metaDescriptionInRightColumn();
			return "Missing " + parameterDescription + " parameter for '" + usedArgumentName + "'.";
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Thrown when {@link ArgumentBuilder#required()} has been specified but the
	 * argument wasn't found in the input arguments
	 */
	static final class MissingRequiredArgumentException extends ArgumentException
	{
		private final Collection<Argument<?>> missingArguments;

		private MissingRequiredArgumentException(final Collection<Argument<?>> missingArguments)
		{
			this.missingArguments = missingArguments;
		}

		@Override
		public String getMessage()
		{
			return "Missing required arguments: " + transform(missingArguments, asFunction(argumentDescriber()));
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	static final class LimitException extends ArgumentException
	{
		private final Limit reason;

		private LimitException(Limit reason)
		{
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

	/**
	 * Used when
	 * "1 2" is expected but
	 * "1 2 3" is given
	 */
	static final class UnexpectedArgumentException extends ArgumentException
	{
		private final String unexpectedArgument;
		private final String previousArgument;

		private UnexpectedArgumentException(@Nonnull final String unexpectedArgument, @Nullable final String previousArgument)
		{
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

	/**
	 * Used when
	 * "--numbers 1 2" is expected but
	 * "--numbers 1 2 --numbers 3 4" is given
	 */
	static final class UnhandledRepeatedArgument extends ArgumentException
	{
		private final Object reason;

		private UnhandledRepeatedArgument(final Object reason)
		{
			this.reason = reason;
		}

		@Override
		public String getMessage()
		{
			return reason.toString();
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
}
