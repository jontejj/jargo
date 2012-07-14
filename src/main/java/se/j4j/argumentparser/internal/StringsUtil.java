package se.j4j.argumentparser.internal;

import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Lists.transform;
import static se.j4j.argumentparser.StringParsers.asFunction;
import static se.j4j.argumentparser.StringParsers.lowerCaseParser;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class StringsUtil
{
	private StringsUtil()
	{
	}

	/**
	 * @param nrOfSpaces number of spaces to put in the created string
	 * @return a string with nrOfSpaces in it
	 */
	@Nonnull
	@CheckReturnValue
	public static String spaces(final int nrOfSpaces)
	{
		return repeat(" ", nrOfSpaces);
	}

	@Nonnull
	public static String surroundWithMarkers(@Nullable String tagToSurround)
	{
		if(tagToSurround == null || tagToSurround.isEmpty())
			return "";
		return "<" + tagToSurround + ">";
	}

	/**
	 * Converts all {@link String}s in <code>strings</code> into lower case using the default
	 * locale.
	 * TODO: add overloaded method that takes in the Locale as well
	 * 
	 * @return a new list with lower case strings in
	 */
	public static List<String> toLowerCase(List<String> strings)
	{
		return transform(strings, asFunction(lowerCaseParser()));
	}
}
