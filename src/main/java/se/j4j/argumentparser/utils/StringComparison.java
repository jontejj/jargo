package se.j4j.argumentparser.utils;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class StringComparison
{
	private StringComparison(){}

	public static int levenshteinDistance(final @Nonnull String one, final @Nonnull String two)
	{
		int m = one.codePointCount(0, one.length());
		int n = two.codePointCount(0, two.length());
		// for all i and j, d[i,j] will hold the Levenshtein distance between
		// the first i characters of s and the first j characters of t;
		// note that d has (m+1)x(n+1) values
		int[][] distances = new int[m+1][n+1];
		for(int i = 0; i <= m; i++)
		{
			distances[i][0] = i;// the distance of any first string to an empty second string
		}
		for(int i = 0; i <= n; i++)
		{
			distances[0][i] = i;// the distance of any second string to an empty first string
		}

		for(int j = 1; j < n; j++)
		{
			for(int i = 1; i < m; i++)
			{
				if(one.codePointAt(i-1) == two.codePointAt(j-1))
				{
					distances[i][j] = distances[i-1][j-1];// no operation required
				}
				else
				{
					int deletion = distances[i-1][j] + 1;
					int insertion = distances[i][j-1] + 1;
					int substitution = distances[i-1][j-1] + 1;
					distances[i][j] = min(deletion, insertion, substitution);
				}
			}
		}
		return distances[m-1][n-1];
	}

	@Nullable
	public static String closestMatch(final @Nonnull String input, final @Nonnull Collection<String> validOptions)
	{
		int minDistance = Integer.MAX_VALUE;
		String bestGuess = null;
		for(String validOption : validOptions)
		{
			int distance = StringComparison.levenshteinDistance(input, validOption);
			if(distance < minDistance)
			{
				minDistance = distance;
				bestGuess = validOption;
			}
		}
		return bestGuess;
	}

	private static int min(final int first, final int...rest)
	{
		int min = first;
		for(int value : rest)
		{
			if(value < min)
			{
				min = value;
			}
		}
		return min;
	}
}
