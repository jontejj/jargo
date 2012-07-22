package se.j4j.argumentparser.internal;

import static java.security.AccessController.doPrivileged;

import java.security.PrivilegedAction;

/**
 * Manages code that needs to be platform specific so that all other code can be platform independent.
 *
 * @formatter.off
 */
public final class Platform
{
	private Platform(){};

	/**
	 * A suitable string to represent newlines on this specific platform
	 */
	public static final String NEWLINE =  doPrivileged(new PrivilegedAction<String>(){
		@Override public String run(){ return System.getProperty("line.separator");}});
}
