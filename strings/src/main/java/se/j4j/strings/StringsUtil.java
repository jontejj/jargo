package se.j4j.strings;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.primitives.Ints.min;

import java.util.List;
import java.util.Locale;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

/**
 * Utilities for working with {@link String}s
 */
public final class StringsUtil
{
	private StringsUtil()
	{
	}

	/**
	 * A suitable string to represent newlines on this specific platform
	 */
	@Nonnull public static final String NEWLINE = System.getProperty("line.separator");

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

	/**
	 * Converts all {@link String}s in {@code strings} into lower case using the default
	 * locale.
	 * TODO: add overloaded method that takes in the Locale as well
	 * 
	 * @return a new (mutable) list with lower case strings in
	 */
	@Nonnull
	@CheckReturnValue
	public static List<String> toLowerCase(List<String> strings)
	{
		List<String> lowerCaseStrings = Lists.newArrayListWithExpectedSize(strings.size());
		for(String string : strings)
		{
			lowerCaseStrings.add(string.toLowerCase(Locale.getDefault()));
		}
		return lowerCaseStrings;
	}

	/**
	 * <pre>
	 * Returns the {@link String} in {@code validOptions} that {@code input} has the shortest
	 * <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">levenshtein distance</a> to.
	 * 
	 * For example when given "stats" as input and "status", "help",
	 * "action" as validOptions, "status" is returned.
	 * 
	 * Current performance characteristics:
	 * n = length of {@code input}
	 * m = average string length of the strings in {@code validOptions}
	 * s = amount of validOptions
	 * 
	 * complexity = n * m * s = O(n<sup>3</sup>)
	 * 
	 * So try to limit the number of valid options...
	 * 
	 * @throws IllegalArgumentException if {@code validOptions} is empty
	 * </pre>
	 */
	@Nonnull
	@CheckReturnValue
	public static String closestMatch(final String input, final Iterable<String> validOptions)
	{
		checkArgument(!isEmpty(validOptions), "No valid options to match the input against");

		int shortestDistance = Integer.MAX_VALUE;
		String bestGuess = null;
		for(String validOption : validOptions)
		{
			int distance = levenshteinDistance(input, validOption);
			if(distance < shortestDistance)
			{
				shortestDistance = distance;
				bestGuess = validOption;
			}
		}
		return bestGuess;
	}

	/**
	 * Returns the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">levenshtein
	 * distance</a> between {@code one} and {@code two}.
	 * 
	 * @see #closestMatch(String, Iterable)
	 */
	public static int levenshteinDistance(final String one, final String two)
	{
		int m = one.codePointCount(0, one.length());
		int n = two.codePointCount(0, two.length());

		if(m == 0)
			return n;
		else if(n == 0)
			return m;

		// for all i and j, d[i,j] will hold the Levenshtein distance between
		// the first i characters of s and the first j characters of t;
		// note that d has (m+1)x(n+1) values
		int[][] distances = new int[m + 1][n + 1];
		for(int i = 0; i <= m; i++)
		{
			// the distance of any first string to an empty second string
			distances[i][0] = i;
		}
		for(int i = 0; i <= n; i++)
		{
			// the distance of any second string to an empty first string
			distances[0][i] = i;
		}

		for(int j = 1; j < n; j++)
		{
			for(int i = 1; i < m; i++)
			{
				/*
				 * TODO: add support for case-insensitive comparisons
				 * int currentCharFromOne = Character.toUpperCase(one.codePointAt(i - 1));
				 * int currentCharFromTwo = Character.toUpperCase(two.codePointAt(j - 1));
				 * if(currentCharFromOne == currentCharFromTwo)
				 * {
				 * }
				 */

				if(one.codePointAt(i - 1) == two.codePointAt(j - 1))
				{
					// no operation required
					distances[i][j] = distances[i - 1][j - 1];
				}
				else
				{
					int deletion = distances[i - 1][j] + 1;
					int insertion = distances[i][j - 1] + 1;
					int substitution = distances[i - 1][j - 1] + 1;

					distances[i][j] = min(deletion, insertion, substitution);
				}
			}
		}
		return distances[m - 1][n - 1];
	}

	/**
	 * Returns {@code number} expressed as a position. For example 0 returns "zeroth", 1 returns
	 * "first" and so forth up to "fifth". Higher positions are described as "6th", "7th" and so on.
	 * 
	 * @throws IllegalArgumentException if {@code number} is negative
	 */
	@Nonnull
	@CheckReturnValue
	public static String numberToPositionalString(int number)
	{
		checkArgument(number >= 0, "Negative numbers don't have positions");
		switch(number)
		{
			case 0:
				return "zeroth";
			case 1:
				return "first";
			case 2:
				return "second";
			case 3:
				return "third";
			case 4:
				return "fourth";
			case 5:
				return "fifth";

		}
		return Integer.toString(number) + "th";
	}

}
