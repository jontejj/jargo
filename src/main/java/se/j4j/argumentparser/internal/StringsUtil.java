package se.j4j.argumentparser.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nonnull;

public final class StringsUtil
{
	private StringsUtil()
	{
	}

	@Nonnull
	public static StringBuilder appendSpaces(final int spacesToAppend, @Nonnull final StringBuilder toBuilder)
	{
		for(int i = 0; i < spacesToAppend; i++)
		{
			toBuilder.append(' ');
		}
		return toBuilder;
	}

	public static List<String> toLowerCase(@Nonnull final Collection<String> strings)
	{
		List<String> lowerCaseStrings = new ArrayList<String>(strings.size());
		for(String s : strings)
		{
			lowerCaseStrings.add(s.toLowerCase(Locale.getDefault()));
		}
		return lowerCaseStrings;
	}
}
