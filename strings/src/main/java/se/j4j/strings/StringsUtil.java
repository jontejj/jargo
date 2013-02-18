package se.j4j.strings;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.repeat;
import static com.google.common.collect.Iterables.isEmpty;
import static se.j4j.strings.StringsUtil.CloseMatch.BY_CLOSEST_MATCH_FIRST;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

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
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * The <a href="http://en.wikipedia.org/wiki/ASCII_tab">ASCII tab</a> (\t) character
	 */
	public static final char TAB = '\t';

	/**
	 * @param numberOfSpaces to put in the created string
	 * @return a string with numberOfSpaces in it
	 */
	@Nonnull
	@CheckReturnValue
	public static String spaces(final int numberOfSpaces)
	{
		return repeat(" ", numberOfSpaces);
	}

	/**
	 * Returns a "       ^" string pointing at the position indicated by {@code indexToPointAt}
	 */
	public static String pointingAtIndex(int indexToPointAt)
	{
		return spaces(indexToPointAt) + "^";
	}

	public static boolean startsWithAndHasMore(String input, String toStartWith)
	{
		return input.startsWith(toStartWith) && input.length() > toStartWith.length();
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
		checkNotNull(input);
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
	 * <pre>
	 * Returns a sorted {@link List} where the first entry is the {@link String} in {@code validOptions} that's closest in terms of
	 * <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">levenshtein distance</a> to {@code input}.
	 * 
	 * For example when given "stats" as input and "status", "staging",
	 * "stage" as validOptions, and 4 as maximumDistance, "status", "stage", "staging" is returned.
	 * 
	 * Only values with a distance less than or equal to {@code maximumDistance} will be included in the result.
	 * 
	 * </pre>
	 */
	@Nonnull
	@CheckReturnValue
	public static List<String> closestMatches(final String input, final Iterable<String> validOptions, int maximumDistance)
	{
		checkNotNull(input);
		if(isEmpty(validOptions))
			return Collections.emptyList();

		List<CloseMatch> closeMatches = Lists.newArrayList();
		for(String validOption : validOptions)
		{
			int distance = levenshteinDistance(input, validOption);
			if(distance <= maximumDistance)
			{
				closeMatches.add(new CloseMatch(validOption, distance));
			}
		}
		Collections.sort(closeMatches, BY_CLOSEST_MATCH_FIRST);
		return Lists.transform(closeMatches, CloseMatch.GET_VALUE);
	}

	static final class CloseMatch
	{
		private final int measuredDistance;
		private final String value;

		private CloseMatch(String validOption, int distance)
		{
			measuredDistance = distance;
			value = validOption;
		}

		static final Comparator<CloseMatch> BY_CLOSEST_MATCH_FIRST = new Comparator<CloseMatch>(){
			@Override
			public int compare(CloseMatch left, CloseMatch right)
			{
				return left.measuredDistance - right.measuredDistance;
			}
		};

		private static final Function<CloseMatch, String> GET_VALUE = new Function<CloseMatch, String>(){
			@Override
			public String apply(@Nonnull CloseMatch input)
			{
				return input.value;
			}
		};
	}

	/**
	 * Returns the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">levenshtein
	 * distance</a> between {@code left} and {@code right}.
	 * 
	 * @see #closestMatch(String, Iterable)
	 */
	public static int levenshteinDistance(final String left, final String right)
	{
		checkNotNull(left);
		checkNotNull(right);

		// a "cleaner" version of the org.apache.commons-lang algorithm which in
		// turn was inspired by http://www.merriampark.com/ldjava.htm
		int leftLength = left.length();
		int rightLength = right.length();

		if(leftLength == 0)
			return rightLength;
		else if(rightLength == 0)
			return leftLength;

		int previousDistances[] = new int[leftLength + 1]; // 'previous' cost array, horizontally
		int distances[] = new int[leftLength + 1]; // cost array, horizontally

		int leftIndex;
		int rightIndex;

		char rightChar;

		for(leftIndex = 0; leftIndex <= leftLength; leftIndex++)
		{
			previousDistances[leftIndex] = leftIndex;
		}

		for(rightIndex = 1; rightIndex <= rightLength; rightIndex++)
		{
			rightChar = right.charAt(rightIndex - 1);
			distances[0] = rightIndex;

			for(leftIndex = 1; leftIndex <= leftLength; leftIndex++)
			{
				int insertionCost = distances[leftIndex - 1] + 1;
				int editCost = previousDistances[leftIndex] + 1;
				int deletionCost = previousDistances[leftIndex - 1];
				if(left.charAt(leftIndex - 1) != rightChar)
				{
					deletionCost++;
				}

				distances[leftIndex] = Ints.min(insertionCost, editCost, deletionCost);
			}

			// Swap current distance counts to 'previous row' distance counts
			int[] temp = previousDistances;
			previousDistances = distances;
			distances = temp;
		}

		// our last action in the above loop was to switch distances and
		// previousDistances, so
		// previousDistances now actually has the most recent cost counts
		return previousDistances[leftLength];
	}

	/**
	 * Returns {@code number} expressed as a position. For example 0 returns
	 * "zeroth", 1 returns "first" and so forth up to "fifth". Higher positions
	 * are described as "6th", "7th" and so on.
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
