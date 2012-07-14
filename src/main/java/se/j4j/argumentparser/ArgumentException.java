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
	private Argument<?> errorneousArgument;

	protected ArgumentException(final ArgumentExceptionCodes errorCode)
	{
		this.errorCode = errorCode;
	}

	public ArgumentException errorneousArgument(final Argument<?> anErrorneousArgument)
	{
		errorneousArgument = anErrorneousArgument;
		return this;
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

	/**
	 * TODO: should this be removed?
	 * 
	 * @return the argument that caused this exception
	 */
	public Argument<?> errorneousArgument()
	{
		return errorneousArgument;
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
