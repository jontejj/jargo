package se.j4j.argumentparser;

import static com.google.common.base.Preconditions.checkNotNull;
import static se.j4j.strings.Descriptions.cache;
import static se.j4j.strings.StringsUtil.NEWLINE;

import javax.annotation.CheckReturnValue;
import javax.annotation.concurrent.Immutable;

import se.j4j.classes.Classes;
import se.j4j.strings.Description;
import se.j4j.strings.Descriptions;

/**
 * Information about a program, printed in {@link Usage} before any {@link Argument}s are described.
 */
@Immutable
final class ProgramInformation
{
	private final Description programName;
	private final String programDescription;

	private ProgramInformation(Description programName, String programDescription)
	{
		this.programName = programName;
		this.programDescription = programDescription;
	}

	/**
	 * <pre>
	 * Automatically tries to get the simple name of the main class by looking at stack traces.
	 * <b>Note:</b>If the main thread has died this method returns "ProgramName".
	 * <b>Note:</b>If there's a restrictive {@link SecurityManager} in place you need to set the
	 * "ProgramName" with {@link CommandLineParser#programName(String)}
	 * </pre>
	 */
	static final ProgramInformation AUTO = new ProgramInformation(cache(new Description(){
		@Override
		public String description()
		{
			return Classes.mainClassName();
		}
	}), "");

	/**
	 * Creates a {@link ProgramInformation} descriptor object for a program with the name
	 * {@code programName}. {@code programName} is used by {@link CommandLineParser#usage()} to tell
	 * the user which program the usage applies to. By default, {@link CommandLineParser#usage()}
	 * uses {@link Classes#mainClassName()} to figure out this name.
	 * 
	 * @param programName is usually "ProgramName" in "java ProgramName --argument parameter".
	 */
	@CheckReturnValue
	static ProgramInformation withProgramName(String programName)
	{
		return new ProgramInformation(Descriptions.withString(programName), "");
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
	ProgramInformation programDescription(String aProgramDescription)
	{
		checkNotNull(aProgramDescription);
		return new ProgramInformation(programName, NEWLINE + aProgramDescription + NEWLINE);
	}

	/**
	 * Resets {@link #withProgramName(String)} to {@code aProgramName} keeping any
	 * {@link #programDescription(String)} previously set
	 * 
	 * @return a newly created {@link ProgramInformation}
	 */
	@CheckReturnValue
	ProgramInformation programName(String aProgramName)
	{
		checkNotNull(aProgramName);
		return new ProgramInformation(Descriptions.withString(aProgramName), programDescription);
	}

	String programName()
	{
		return programName.description();
	}

	String programDescription()
	{
		return programDescription;
	}

	@Override
	public String toString()
	{
		return programName() + ":" + programDescription();
	}
}
