package se.j4j.argumentparser;

import static se.j4j.argumentparser.internal.Platform.NEWLINE;

import java.io.Serializable;

public class ArgumentException extends Exception
{
	// TODO: to enable proper behavior when serialized these needs to
	// be transient (or Serializable and the usage needs to be transferred as a string
	private CommandLineParser originParser;

	protected ArgumentException()
	{
	}

	ArgumentException originatedFrom(final CommandLineParser theParserThatTriggeredMe)
	{
		originParser = theParserThatTriggeredMe;
		return this;
	}

	public String getUsage(String programName)
	{
		if(originParser == null)
			throw new IllegalStateException("No originParser set for ArgumentException. No usage available for " + programName);
		return originParser.usage(programName);
	}

	public String getMessageAndUsage(String programName)
	{
		return getMessage() + NEWLINE + NEWLINE + getUsage(programName);
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;

}
