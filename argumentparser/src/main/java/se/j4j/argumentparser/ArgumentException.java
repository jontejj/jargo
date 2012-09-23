package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkState;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.Serializable;

import se.j4j.texts.Texts;

/**
 * Indicates that something went wrong in a {@link CommandLineParser}. The typical remedy action is
 * to present {@link #getMessageAndUsage(String)} to the user so he is informed about what he did
 * wrong.
 */
public abstract class ArgumentException extends Exception
{
	// TODO: to enable proper behavior when serialized these needs to
	// be transient (or Serializable and the usage needs to be transferred as a string
	private transient CommandLineParser originParser;
	private String originArgumentName;

	protected ArgumentException()
	{
	}

	/**
	 * Alias for {@link #initCause(Throwable)}.<br>
	 * Added simply because {@code withMessage("Message").andCause(exception)} flows better.
	 */
	public final ArgumentException andCause(Throwable cause)
	{
		initCause(cause);
		return this;
	}

	/**
	 * Returns a usage string explaining how to use the {@link CommandLineParser} that caused this
	 * exception. To also get the error message detailing the erroneous argument use
	 * {@link #getMessageAndUsage(String)} instead.
	 */
	public final String getUsage(String programName)
	{
		checkState(originParser != null, Texts.NO_USAGE_AVAILABLE, programName);

		return originParser.usage(programName);
	}

	/**
	 * Returns a usage string explaining how to use the {@link CommandLineParser} that caused this
	 * exception, prepended with an error message detailing the erroneous argument.
	 */
	public final String getMessageAndUsage(String programName)
	{
		String message = getMessage(originArgumentName);
		return message + NEWLINE + NEWLINE + getUsage(programName);
	}

	/**
	 * Returns why this exception occurred.
	 * 
	 * @param argumentNameOrCommandName if the argument that caused this exception to happen
	 *            is part of a {@link Command} and is indexed then the command name used to trigger
	 *            the command is given,
	 *            otherwise the argument name that was used on the command line is used.
	 */
	protected abstract String getMessage(String argumentNameOrCommandName);

	/**
	 * Marked as final as the {@link #getMessage(String)} should be implemented instead
	 */
	@Override
	public final String getMessage()
	{
		return getMessage(originArgumentName);
	}

	final ArgumentException originatedFrom(final CommandLineParser theParserThatTriggeredMe)
	{
		originParser = theParserThatTriggeredMe;
		return this;
	}

	final void originatedFromArgumentName(String argumentNameThatTriggeredMe)
	{
		originArgumentName = argumentNameThatTriggeredMe;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
