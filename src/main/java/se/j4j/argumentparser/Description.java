package se.j4j.argumentparser;

import javax.annotation.Nonnull;

/**
 * Allows for lazily created descriptions and the possible performance
 * optimization that the string is not constructed if it's not used.
 * If you already have a created {@link String} it's recommended to just use that instead.
 */
public interface Description
{
	/**
	 * @return a description
	 */
	@Nonnull
	String description();
}
