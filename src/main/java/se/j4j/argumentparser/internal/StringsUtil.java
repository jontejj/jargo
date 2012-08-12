package se.j4j.argumentparser.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.transform;
import static com.google.common.primitives.Ints.min;
import static se.j4j.argumentparser.StringParsers.asFunction;
import static se.j4j.argumentparser.StringParsers.lowerCaseParser;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

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

	/**
	 * Converts all {@link String}s in {@code strings} into lower case using the default
	 * locale.
	 * TODO: add overloaded method that takes in the Locale as well
	 * 
	 * @return a new list with lower case strings in
	 */
	@Nonnull
	@CheckReturnValue
	public static List<String> toLowerCase(List<String> strings)
	{
		return transform(strings, asFunction(lowerCaseParser()));
	}

	/**
	 * <pre>
	 * Returns the {@link String} in {@code validOptions} that {@code input} has the shortest
	 * <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">levenshtein distance</a> to.
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
	 * @throws IllegalArgumentException if there's no valid options to match input against
	 * </pre>
	 */
	@Nonnull
	@CheckReturnValue
	public static String closestMatch(@Nonnull final String input, @Nonnull final Iterable<String> validOptions)
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

	public static int levenshteinDistance(@Nonnull final String one, @Nonnull final String two)
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

	public static String numberToPositionalString(int number)
	{
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
