package se.j4j.argumentparser;

import javax.annotation.Nonnull;

public interface StringSplitter
{
	@Nonnull
	Iterable<String> split(@Nonnull CharSequence input);

	/**
	 * Shown as help for understanding how the split is done in usage texts
	 * 
	 * @return the description
	 */
	@Nonnull
	String description();
}
