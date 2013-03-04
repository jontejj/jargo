/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.softhouse.jargo;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.softhouse.comeon.strings.StringsUtil.numberToPositionalString;

import java.io.Serializable;
import java.util.Collection;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import se.softhouse.comeon.strings.Description;
import se.softhouse.comeon.strings.Descriptions;
import se.softhouse.comeon.strings.Descriptions.SerializableDescription;
import se.softhouse.jargo.ArgumentBuilder.ArgumentSettings;
import se.softhouse.jargo.CommandLineParserInstance.ArgumentIterator;
import se.softhouse.jargo.internal.Texts.UserErrors;

/**
 * Gives you static access for creating {@link ArgumentException}s.<br>
 * Remember that created exceptions are simply returned <b>not thrown</b>.
 */
public final class ArgumentExceptions
{
	private ArgumentExceptions()
	{
	}

	/**
	 * The most simple version of {@link ArgumentException}s, it simply prints the result of
	 * {@link #toString()} for {@code message} as the message. Use
	 * {@link #withMessage(Object, Throwable)} if you have a cause.
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException withMessage(final Object message)
	{
		return new SimpleArgumentException(Descriptions.toString(message));
	}

	/**
	 * Like {@link #withMessage(Object)} but with a {@code cause}
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException withMessage(final Object message, Throwable cause)
	{
		return new SimpleArgumentException(Descriptions.toString(message)).andCause(cause);
	}

	/**
	 * {@link Description} based version of {@link #withMessage(Object)}
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException withMessage(Description message)
	{
		return new SimpleArgumentException(message);
	}

	/**
	 * Like {@link #withMessage(Description)} but with a {@code cause}
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException withMessage(Description message, Throwable cause)
	{
		return new SimpleArgumentException(message).andCause(cause);
	}

	private static final class SimpleArgumentException extends ArgumentException
	{
		private final SerializableDescription message;

		private SimpleArgumentException(final Description message)
		{
			this.message = Descriptions.asSerializable(message);
		}

		@Override
		protected String getMessage(String argumentNameOrcommandName)
		{
			return message.description();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Converts any {@link Throwable} into an {@link ArgumentException}.
	 * Uses the detail message of {@code exceptionToWrap} as it's own detail message.
	 * {@code exceptionToWrap} is also set as the cause of the created exception.
	 */
	@CheckReturnValue
	@Nonnull
	public static ArgumentException wrapException(final Throwable exceptionToWrap)
	{
		checkNotNull(exceptionToWrap);
		return new WrappedArgumentException(exceptionToWrap);
	}

	private static final class WrappedArgumentException extends ArgumentException
	{
		private final Throwable wrappedException;

		WrappedArgumentException(final Throwable wrappedException)
		{
			this.wrappedException = wrappedException;
			initCause(wrappedException);
		}

		@Override
		protected String getMessage(String argumentNameOrcommandName)
		{
			return wrappedException.getMessage();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	};

	/**
	 * @param cause the checked exception that will be used as the cause (and message) of the
	 *            returned exception
	 * @return an unchecked exception that uses {@code cause#getMessage()} as it's own message
	 */
	@CheckReturnValue
	@Nonnull
	static IllegalArgumentException asUnchecked(final ArgumentException cause)
	{
		checkNotNull(cause);
		return new UncheckedArgumentException(cause);
	}

	private static final class UncheckedArgumentException extends IllegalArgumentException
	{
		UncheckedArgumentException(final ArgumentException cause)
		{
			super(cause);
		}

		@Override
		public String getMessage()
		{
			return getCause().getMessage();
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Thrown when {@link ArgumentBuilder#required()} has been specified but the
	 * argument wasn't found in the input arguments
	 * 
	 * @param missingArguments a collection of all the missing arguments
	 */
	@CheckReturnValue
	@Nonnull
	static ArgumentException forMissingArguments(final Collection<Argument<?>> missingArguments)
	{
		return new MissingRequiredArgumentException(missingArguments);
	}

	private static final class MissingRequiredArgumentException extends ArgumentException
	{
		private final String missingArguments;

		private MissingRequiredArgumentException(final Collection<Argument<?>> missingArguments)
		{
			this.missingArguments = missingArguments.toString();
		}

		@Override
		protected String getMessage(String argumentNameOrcommandName)
		{
			if(isCausedByCommand(argumentNameOrcommandName))
				return String.format(UserErrors.MISSING_COMMAND_ARGUMENTS, argumentNameOrcommandName, missingArguments);
			return String.format(UserErrors.MISSING_REQUIRED_ARGUMENTS, missingArguments);
		}

		private boolean isCausedByCommand(@Nullable String argumentNameOrcommandName)
		{
			return argumentNameOrcommandName != null;
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
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
		checkNotNull(unhandledArgument);
		return new SimpleArgumentException(Descriptions.format(UserErrors.DISALLOWED_REPETITION, unhandledArgument));
	}

	/**
	 * Used when
	 * "-p 8080" is expected but
	 * "-p" is given <br>
	 * Prints "Missing <Integer> parameter for -p"
	 * 
	 * @param argumentWithMissingParameter the -p argument in this case
	 */
	@CheckReturnValue
	@Nonnull
	static ArgumentException forMissingParameter(ArgumentSettings argumentWithMissingParameter)
	{
		return new MissingParameterException(argumentWithMissingParameter);
	}

	static final class MissingParameterException extends ArgumentException
	{
		private final String parameterDescription;

		private MissingParameterException(ArgumentSettings argumentWithMissingParameter)
		{
			this.parameterDescription = argumentWithMissingParameter.metaDescriptionInRightColumn();
		}

		@Override
		protected String getMessage(String argumentNameOrcommandName)
		{
			return String.format(UserErrors.MISSING_PARAMETER, parameterDescription, argumentNameOrcommandName);
		}

		String parameterDescription()
		{
			return parameterDescription;
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Used when
	 * "-p 8080 7080" is expected but
	 * "-p 8080" is given<br>
	 * Prints "Missing second <Integer> parameter for -p"
	 * 
	 * @param parameterDescription the <Integer> argument in this case
	 * @param missingIndex 1 in this case
	 */
	@CheckReturnValue
	@Nonnull
	static ArgumentException forMissingNthParameter(String parameterDescription, int missingIndex)
	{
		checkNotNull(parameterDescription);
		return new MissingNthParameterException(parameterDescription, missingIndex);
	}

	private static final class MissingNthParameterException extends ArgumentException
	{
		private final String parameterDescription;
		private final int missingIndex;

		private MissingNthParameterException(String parameterDescription, int missingIndex)
		{
			this.parameterDescription = parameterDescription;
			this.missingIndex = missingIndex;
		}

		@Override
		protected String getMessage(String argumentNameOrCommandName)
		{
			return String.format(	UserErrors.MISSING_NTH_PARAMETER, numberToPositionalString(missingIndex + 1), parameterDescription,
									argumentNameOrCommandName);
		}

		/**
		 * For {@link Serializable}
		 */
		private static final long serialVersionUID = 1L;
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
	static ArgumentException forUnexpectedArgument(final ArgumentIterator arguments)
	{
		String unexpectedArgument = arguments.previous();
		String previousArgument = null;
		if(arguments.hasPrevious())
		{
			previousArgument = arguments.previous();
		}

		return new UnexpectedArgumentException(unexpectedArgument, previousArgument);
	}

	static final class UnexpectedArgumentException extends ArgumentException
	{
		private final String unexpectedArgument;
		private final String previousArgument;

		private UnexpectedArgumentException(final String unexpectedArgument, @Nullable final String previousArgument)
		{
			this.unexpectedArgument = unexpectedArgument;
			this.previousArgument = previousArgument;
		}

		@Override
		protected String getMessage(String argumentNameOrcommandName)
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
}
