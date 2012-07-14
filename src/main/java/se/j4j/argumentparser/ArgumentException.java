package se.j4j.argumentparser;

import java.io.Serializable;

import se.j4j.argumentparser.ArgumentExceptions.ArgumentExceptionCodes;
import se.j4j.argumentparser.internal.Lines;

public class ArgumentException extends Exception
{
	private final ArgumentExceptionCodes errorCode;

	// TODO: to enable proper behavior when serialized these needs to
	// be transient (or Serializable and the usage needs to be transferred as a string
	private CommandLineParser originParser;

	protected ArgumentException(final ArgumentExceptionCodes errorCode)
	{
		this.errorCode = errorCode;
	}

	void setOriginParser(final CommandLineParser theParserThatTriggeredMe)
	{
		originParser = theParserThatTriggeredMe;
	}

	public String getUsage(String programName)
	{
		if(originParser == null)
			throw new IllegalStateException("No originParser set for ArgumentException. No usage available for " + programName);
		return originParser.usage(programName);
	}

	public String getMessageAndUsage(String programName)
	{
		return getMessage() + Lines.NEWLINE + Lines.NEWLINE + getUsage(programName);
	}

	public ArgumentExceptionCodes code()
	{
		return errorCode;
	}

	/**
	 * For {@link Serializable}
	 */
	private static final long serialVersionUID = 1L;

}
