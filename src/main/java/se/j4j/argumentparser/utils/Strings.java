package se.j4j.argumentparser.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public final class Strings
{
	private Strings(){}

	@Nonnull
	public static StringBuilder appendSpaces(final int spacesToAppend, final @Nonnull StringBuilder toBuilder)
	{
		for(int i = 0; i < spacesToAppend; i++)
		{
			toBuilder.append(' ');
		}
		return toBuilder;
	}

	public static List<String> toLowerCase(final Collection<String> strings)
	{
		List<String> lowerCaseStrings = new ArrayList<>(strings.size());
		for(String s : strings)
		{
			lowerCaseStrings.add(s.toLowerCase());
		}
		return lowerCaseStrings;
	}
}
