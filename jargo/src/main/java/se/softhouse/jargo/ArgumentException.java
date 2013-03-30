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
import static se.softhouse.common.strings.StringsUtil.NEWLINE;

import java.io.Serializable;

import javax.annotation.concurrent.NotThreadSafe;

import se.softhouse.common.strings.Description;
import se.softhouse.common.strings.Descriptions;
import se.softhouse.common.strings.Descriptions.SerializableDescription;
import se.softhouse.jargo.internal.Texts.ProgrammaticErrors;
import se.softhouse.jargo.internal.Texts.UsageTexts;

/**
 * Indicates that something went wrong in a {@link CommandLineParser#parse(String...) parsing}.
 * Typical causes include:
 * <ul>
 * <li>Missing parameters</li>
 * <li>Unknown arguments, if it's close to a known argument the error message will contain
 * suggestions</li>
 * <li>Missing required arguments</li>
 * <li>Invalid arguments, thrown from {@link StringParser#parse(String, java.util.Locale) parse}</li>
 * <li>Repetition of argument that hasn't specified {@link ArgumentBuilder#repeated() repeated}</li>
 * </ul>
 * The typical remedy action is to present {@link #getMessageAndUsage()} to the user so he is
 * informed about what he did wrong. <br>
 * As all {@link Exception}s should be, {@link ArgumentException} is {@link Serializable} so usage
 * is available after deserialization.
 */
@NotThreadSafe
public abstract class ArgumentException extends Exception
{
	/**
	 * The {@link Usage} explaining how to avoid this exception
	 */
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
		// TODO(jontejj): jack into the uncaughtExceptionHandler and remove stacktraces? Potentially
		// very annoying feature...
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
		if(usageText != null)
			return this; // Don't overwrite if the user has specified a custom Usage
		usageText = Descriptions.asSerializable(new Description(){
			@Override
			public String description()
			{
				return usage.toString();
			}
		});
		return this;
	}

	final ArgumentException withUsedArgumentName(String argumentNameThatTriggeredMe)
	{
		usedArgumentName = argumentNameThatTriggeredMe;
		return this;
	}

	/**
	 * By default ". See usage for --author for proper values." is appended to the end of the error
	 * message if {@link #getMessageAndUsage()} is used. This method overrides that to be
	 * {@code usageReference} instead.
	 */
	void withUsageReference(final String usageReference)
	{
		usageReferenceName = checkNotNull(usageReference);
	}

	ArgumentException withUsageReference(final Argument<?> usageReference)
	{
		if(usageReferenceName != null)
			return this; // Don't overwrite if the user has specified a custom reference
		usageReferenceName = String.format(UsageTexts.USAGE_REFERENCE, usageReference);
		return this;
	}

	private String usageReference()
	{
		return usageReferenceName;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;
}
