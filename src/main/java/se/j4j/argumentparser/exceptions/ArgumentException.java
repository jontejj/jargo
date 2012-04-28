package se.j4j.argumentparser.exceptions;

import java.io.Serializable;

import se.j4j.argumentparser.Argument;
import se.j4j.argumentparser.ArgumentParser;
import se.j4j.argumentparser.utils.Lines;

public class ArgumentException extends Exception
{
	private final ArgumentExceptionCodes errorCode;
	private Argument<?> errorneousArgument;
	private ArgumentParser originParser;

	protected ArgumentException(final ArgumentExceptionCodes errorCode)
	{
		this.errorCode = errorCode;
	}

	public ArgumentException errorneousArgument(final Argument<?> anErrorneousArgument)
	{
		errorneousArgument = anErrorneousArgument;
		return this;
	}

	public void setOriginParser(final ArgumentParser theParserThatTriggeredMe)
	{
		originParser = theParserThatTriggeredMe;
	}

	public static ArgumentException create(final ArgumentExceptionCodes errorCode)
	{
		return new ArgumentException(errorCode);
	}

	@Override
	public String getMessage()
	{
		return "Error code: " + errorCode;
	}

	public String getUsage(String programName)
	{
		String usage = originParser != null ? originParser.usage(programName).toString() : "";
		return usage;
	}

	public String getMessageAndUsage(String programName)
	{
		return getMessage() + Lines.NEWLINE + getUsage(programName);
	}

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
