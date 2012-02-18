package se.j4j.argumentparser.exceptions;

public enum ArgumentExceptionCodes
{
	//TODO: these should take in an argument describing the exact cause

	/** Used when
	 * "-p 8080" is expected but
	 * "-p" is given
	 * */
	MISSING_PARAMETER,

	/**
	 * Used when
	 * "1 2" is expected but
	 * "1 2 3" is given
	 * and {@link ArgumentParser#throwOnUnexpectedArgument()} has been called
	 */
	UNHANDLED_PARAMETER,

	/**
	 * Thrown when {@link Argument#required()} has been specified but the argument wasn't found in the input arguments
	 */
	MISSING_REQUIRED_PARAMETER,

	/**
	 * Used when
	 * "--numbers 1 2" is expected but
	 * "--numbers 1 2 --numbers 3 4" is given
	 */
	UNHANDLED_REPEATED_PARAMETER,

	/**
	 * May be thrown by {@link Argument#parse(java.util.ListIterator)}
	 * which considers it's received argument to be invalid
	 */
	INVALID_PARAMTER

}
