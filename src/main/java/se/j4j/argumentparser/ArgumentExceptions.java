package se.j4j.argumentparser;

import static se.j4j.argumentparser.internal.StringsUtil.numberToPositionalString;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.CommandLineParser.ArgumentIterator;
import se.j4j.argumentparser.internal.Texts;

/**
 * Gives you static access for creating {@link ArgumentException}s.<br>
 * Remember that created exceptions are simply returned <b>not thrown</b>
 */
public final class ArgumentExceptions
{
	private ArgumentExceptions()
	{
	}

	// TODO , see usage for '-i' for proper values.

	/**
	 * The most simple version of {@link ArgumentException}s, it simply prints the result of
	 * {@link #toString()} for {@code message} as the message.
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException withMessage(final Object message)
	{
		return new SimpleArgumentException(Descriptions.toString(message));
	}

	/**
	 * {@link Description} based version of {@link #withMessage(Object)}
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException withMessage(@Nonnull Description message)
	{
		return new SimpleArgumentException(message);
	}

	/**
	 * @param cause the checked exception that will be used as the cause (and message) of the
	 *            returned exception
	 * @return an unchecked exception that uses {@code cause#getMessage()} as it's own message
	 */
	@CheckReturnValue
	@Nonnull
	static IllegalArgumentException asUnchecked(@Nonnull final ArgumentException cause)
	{
		return new UncheckedArgumentException(cause);
	}

	/**
	 * Thrown when {@link ArgumentBuilder#required()} has been specified but the
	 * argument wasn't found in the input arguments
	 * 
	 * @param missingArguments a collection of all the missing arguments
	 * @param parser the parser that is missing the arguments
	 */
	@CheckReturnValue
	@Nonnull
	static MissingRequiredArgumentException forMissingArguments(final Collection<Argument<?>> missingArguments, final CommandLineParser parser)
	{
		MissingRequiredArgumentException exception = new MissingRequiredArgumentException(missingArguments);
		exception.originatedFrom(parser);
		return exception;
	}

	/**
	 * Used when
	 * "--number 1" is expected but
	 * "--number 1 --number 2" is given
	 * 
	 * @param unhandledArgument the argument --number in this case
	 */
	@CheckReturnValue
	@Nonnull
	static ArgumentException forUnallowedRepetitionArgument(final String unhandledArgument)
	{
		return new SimpleArgumentException(Descriptions.format(Texts.UNALLOWED_REPETITION, unhandledArgument));
	}

	/**
	 * Used when
	 * "-p 8080" is expected but
	 * "-p" is given <br>
	 * Prints "Missing &lt;Integer&gt; parameter for -p"
	 * 
	 * @param argumentWithMissingParameter the -p argument in this case
	 */
	@CheckReturnValue
	@Nonnull
	static MissingParameterException forMissingParameter(ArgumentSettings argumentWithMissingParameter)
	{
		return new MissingParameterException(argumentWithMissingParameter);
	}

	/**
	 * Used when
	 * "-p 8080 7080" is expected but
	 * "-p 8080" is given<br>
	 * Prints "Missing second &lt;Integer&gt; parameter for -p"
	 * 
	 * @param argumentWithMissingParameter the -p argument in this case
	 * @param missingIndex 1 in this case
	 */
	@CheckReturnValue
	@Nonnull
	static MissingNthParameterException forMissingNthParameter(ArgumentSettings argumentWithMissingParameter, int missingIndex)
	{
		return new MissingNthParameterException(argumentWithMissingParameter, missingIndex);
	}

	/**
	 * Used when
	 * "1 2" is expected but
	 * "1 2 3" is given<br>
	 * Prints "Unexpected argument: 3, previous argument: 2"
	 * 
	 * @param arguments used to print the unexpected argument, 3 in this case, 2 is also printed to
	 *            further pinpoint where 3 is situated
	 */
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

	static final class MissingParameterException extends ArgumentException
	{
		final ArgumentSettings argumentWithMissingParameter;

		private MissingParameterException(ArgumentSettings argumentWithMissingParameter)
		{
			this.argumentWithMissingParameter = argumentWithMissingParameter;
		}

		@Override
		public String getMessage(String argumentNameOrcommandName)
		{
			String parameterDescription = argumentWithMissingParameter.metaDescriptionInRightColumn();
			return String.format(Texts.MISSING_PARAMETER, parameterDescription, argumentNameOrcommandName);
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	static final class MissingNthParameterException extends ArgumentException
	{
		private final ArgumentSettings argumentWithMissingParameter;
		private final int missingIndex;

		private MissingNthParameterException(ArgumentSettings argumentWithMissingParameter, int missingIndex)
		{
			this.argumentWithMissingParameter = argumentWithMissingParameter;
			this.missingIndex = missingIndex;
		}

		@Override
		public String getMessage(String argumentNameOrcommandName)
		{
			String parameterDescription = argumentWithMissingParameter.metaDescriptionInRightColumn();
			return String.format(	Texts.MISSING_NTH_PARAMETER, numberToPositionalString(missingIndex + 1), parameterDescription,
									argumentNameOrcommandName);
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	static final class MissingRequiredArgumentException extends ArgumentException
	{
		private final Collection<Argument<?>> missingArguments;

		private MissingRequiredArgumentException(final Collection<Argument<?>> missingArguments)
		{
			this.missingArguments = missingArguments;
		}

		@Override
		public String getMessage(String argumentNameOrcommandName)
		{
			if(argumentNameOrcommandName != null)
				return String.format(Texts.MISSING_COMMAND_ARGUMENTS, argumentNameOrcommandName, missingArguments);
			return String.format(Texts.MISSING_REQUIRED_ARGUMENTS, missingArguments);
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

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
		public String getMessage(String argumentNameOrcommandName)
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

	static final class SimpleArgumentException extends ArgumentException
	{
		private final Description message;

		private SimpleArgumentException(final Description message)
		{
			this.message = message;
		}

		@Override
		public String getMessage(String argumentNameOrcommandName)
		{
			return message.description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	private static final class UncheckedArgumentException extends IllegalArgumentException
	{
		private final ArgumentException cause;

		UncheckedArgumentException(final ArgumentException cause)
		{
			super(cause);
			this.cause = cause;
		}

		@Override
		public String getMessage()
		{
			return cause.getMessage();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	};
}
