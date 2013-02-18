package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.strings.StringsUtil.NEWLINE;

import java.io.Serializable;

import javax.annotation.concurrent.NotThreadSafe;

import se.j4j.argumentparser.ArgumentBuilder.ArgumentSettings;
import se.j4j.argumentparser.internal.Texts.ProgrammaticErrors;
import se.j4j.argumentparser.internal.Texts.UsageTexts;
import se.j4j.strings.Description;
import se.j4j.strings.Descriptions;
import se.j4j.strings.Descriptions.SerializableDescription;

/**
 * Indicates that something went wrong in a {@link CommandLineParser}. The typical remedy action is
 * to present {@link #getMessageAndUsage()} to the user so he is informed about what he did
 * wrong. As all {@link Exception}s should be, {@link ArgumentException} is {@link Serializable} so
 * usage is available after deserialization.
 */
@NotThreadSafe
public abstract class ArgumentException extends Exception
{
	private SerializableDescription usageText = null;

	/**
	 * The used name, one of the strings passed to {@link ArgumentBuilder#names(String...)}
	 */
	private String usedArgumentName = null;

	/**
	 * The first name passed to {@link ArgumentBuilder#names(String...)}. Used to print a reference
	 * to the explanation of the erroneous argument. Used instead of usedArgumentName as the first
	 * name is the leftmost in the listings.
	 */
	private String usageReferenceName = null;

	protected ArgumentException()
	{
	}

	/**
	 * Alias for {@link #initCause(Throwable)}.<br>
	 * Added simply because {@code withMessage("Message").andCause(exception)} flows better.
	 */
	public final ArgumentException andCause(Throwable cause)
	{
		initCause(checkNotNull(cause));
		return this;
	}

	/**
	 * Returns a usage string explaining how to use the {@link CommandLineParser} that
	 * caused this exception, prepended with an error message detailing the erroneous argument and,
	 * if applicable, a reference to the usage where the user can read about acceptable input.
	 */
	public final String getMessageAndUsage()
	{
		// TODO: jack into the uncaughtExceptionHandler and remove stacktraces?
		String message = getMessage(usedArgumentName);
		return message + usageReference() + NEWLINE + NEWLINE + getUsage();
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
	 * <pre>
	 * {@inheritDoc}
	 * 
	 * Marked as final as the {@link #getMessage(String)} should be implemented instead
	 * </pre>
	 */
	@Override
	public final String getMessage()
	{
		// TODO: Reference to usageReference even without usage?
		return getMessage(usedArgumentName);
	}

	/**
	 * Returns a usage string explaining how to use the {@link CommandLineParser} that
	 * caused this exception.
	 */
	private String getUsage()
	{
		checkNotNull(usageText, ProgrammaticErrors.NO_USAGE_AVAILABLE);
		return usageText.description();
	}

	final ArgumentException withUsage(final Usage usage)
	{
		usageText = Descriptions.asSerializable(new Description(){
			@Override
			public String description()
			{
				return usage.toString();
			}
		});
		return this;
	}

	final ArgumentException originatedFromArgumentName(String argumentNameThatTriggeredMe)
	{
		usedArgumentName = argumentNameThatTriggeredMe;
		return this;
	}

	final ArgumentException originatedFrom(final ArgumentSettings argument)
	{
		usageReferenceName = argument.toString();
		return this;
	}

	private String usageReference()
	{
		if(hasUsageReference())
			return String.format(UsageTexts.USAGE_REFERENCE, usageReferenceName);
		return "";
	}

	private boolean hasUsageReference()
	{
		return usageReferenceName != null;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
