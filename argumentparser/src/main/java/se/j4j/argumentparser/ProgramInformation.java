package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.strings.StringsUtil.NEWLINE;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

/**
 * Information about a program, printed in {@link Usage} before any {@link Argument}s are described.
 */
@Immutable
public final class ProgramInformation
{
	private final String programName;
	private final String programDescription;

	private ProgramInformation(String programName, String programDescription)
	{
		this.programName = programName;
		this.programDescription = programDescription;
	}

	/**
	 * Creates a {@link ProgramInformation} descriptor object for a program with the name
	 * {@code programName}. {@code programName} is used by
	 * {@link CommandLineParser#usage(ProgramInformation)} to tell the user which program the usage
	 * applies to.
	 * 
	 * @param programName is usually "ProgramName" in "java -jar ProgramName --argument parameter".
	 */
	@CheckReturnValue
	public static ProgramInformation programName(String programName)
	{
		checkNotNull(programName);
		return new ProgramInformation(programName, "");
	}

	/**
	 * Sets the optional {@code programDescription} that's used to describe what the program as a
	 * whole does.
	 * 
	 * @param aProgramDescription the description to print at the top of the usage texts, just below
	 *            the {@link #programName(String)}.
	 * @return a newly created {@link ProgramInformation}
	 */
	@CheckReturnValue
	public ProgramInformation programDescription(String aProgramDescription)
	{
		checkNotNull(aProgramDescription);
		return new ProgramInformation(programName, NEWLINE + aProgramDescription + NEWLINE);
	}

	String programName()
	{
		// TODO: maybe print one line with all options, .includeOneLinerInUsage()
		return programName;
	}

	String programDescription()
	{
		return programDescription;
	}
}
