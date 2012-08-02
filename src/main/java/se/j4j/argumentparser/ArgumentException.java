package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkState;
import static se.j4j.argumentparser.internal.Platform.NEWLINE;

import java.io.Serializable;

import javax.annotation.Nonnull;

import se.j4j.argumentparser.internal.Texts;

public abstract class ArgumentException extends Exception
{
	// TODO: to enable proper behavior when serialized these needs to
	// be transient (or Serializable and the usage needs to be transferred as a string
	private CommandLineParser originParser;
	private String originArgumentName;

	protected ArgumentException()
	{
	}

	ArgumentException originatedFrom(final CommandLineParser theParserThatTriggeredMe)
	{
		originParser = theParserThatTriggeredMe;
		return this;
	}

	void originatedFromArgumentName(String argumentNameThatTriggeredMe)
	{
		originArgumentName = argumentNameThatTriggeredMe;
	}

	public ArgumentException andCause(Throwable cause)
	{
		initCause(cause);
		return this;
	}

	public final String getUsage(String programName)
	{
		checkState(originParser != null, Texts.NO_USAGE_AVAILABLE, programName);

		return originParser.usage(programName);
	}

	public final String getMessageAndUsage(@Nonnull String programName)
	{
		String message = getMessage(originArgumentName);
		return message + NEWLINE + NEWLINE + getUsage(programName);
	}

	/**
	 * Marked as final as the {@link #getMessage(String)} should be implemented instead
	 */
	@Override
	public final String getMessage()
	{
		return getMessage(originArgumentName);
	}

	/**
	 * Returns why this exception occurred.
	 * 
	 * @param argumentNameOrcommandName if the argument that caused this exception to happen
	 *            is part of a {@link Command} and is indexed then the command name used to trigger
	 *            the command is given,
	 *            otherwise the argument name that was used on the command line is used.
	 */
	protected abstract String getMessage(@Nonnull String argumentNameOrcommandName);

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
