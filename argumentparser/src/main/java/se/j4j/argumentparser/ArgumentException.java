package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkState;
import static se.j4j.strings.Descriptions.asSerializable;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.Serializable;

import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UsageTexts;
import se.j4j.strings.Descriptions;
import se.j4j.strings.Descriptions.SerializableDescription;

/**
 * Indicates that something went wrong in a {@link CommandLineParser}. The typical remedy action is
 * to present {@link #getMessageAndUsage(String)} to the user so he is informed about what he did
 * wrong.
 */
@NotThreadSafe
public abstract class ArgumentException extends Exception
{
	// TODO: to enable proper behavior when serialized these needs to
	// be transient (or Serializable and the usage needs to be transferred as a string
	private transient CommandLineParser originParser;
	private String usedArgumentName = null;
	private SerializableDescription usageReference = Descriptions.EMPTY_STRING;

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
	 * exception, prepended with an error message detailing the erroneous argument.
	 */
	public final String getMessageAndUsage(String programName)
	{
		String message = getMessage(usedArgumentName);
		return message + usageReference() + NEWLINE + NEWLINE + getUsage(programName);
	}

	/**
	 * Returns why this exception occurred.
	 * 
	 * @param referenceName the argument name that was used on the command line.
	 *            If indexed the {@link ArgumentBuilder#metaDescription(String)} is given.
	 *            Furthermore if the argument is both indexed and part of a {@link Command} the
	 *            command name used to trigger the command is given. Alas never null.
	 */
	protected abstract String getMessage(String referenceName);

	/**
	 * Marked as final as the {@link #getMessage(String)} should be implemented instead
	 */
	@Override
	public final String getMessage()
	{
		return getMessage(usedArgumentName);
	}

	/**
	 * Returns a usage string explaining how to use the {@link CommandLineParser} that caused this
	 * exception.
	 */
	private String getUsage(String programName)
	{
		checkState(originParser != null, ProgrammaticErrors.NO_USAGE_AVAILABLE, programName);

		return originParser.usage(programName);
	}

	final ArgumentException originatedFrom(final CommandLineParser theParserThatTriggeredMe)
	{
		originParser = theParserThatTriggeredMe;
		return this;
	}

	final ArgumentException originatedFromArgumentName(String argumentNameThatTriggeredMe)
	{
		usedArgumentName = argumentNameThatTriggeredMe;
		return this;
	}

	final ArgumentException originatedFrom(final ArgumentSettings argument)
	{
		usageReference = asSerializable(Descriptions.toString(argument));
		return this;
	}

	private String usageReference()
	{
		if(hasUsageReference())
			return String.format(UsageTexts.USAGE_REFERENCE, usageReference);
		return "";
	}

	private boolean hasUsageReference()
	{
		return usageReference != Descriptions.EMPTY_STRING;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
