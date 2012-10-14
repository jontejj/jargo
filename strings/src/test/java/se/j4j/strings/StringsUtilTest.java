package se.j4j.strings;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.strings.StringsUtil.closestMatch;
import static se.j4j.strings.StringsUtil.closestMatches;
import static se.j4j.strings.StringsUtil.numberToPositionalString;
import static se.j4j.strings.StringsUtil.spaces;
import static se.j4j.strings.StringsUtil.toLowerCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import se.j4j.testlib.Explanation;
import se.j4j.testlib.UtilityClassTester;

import com.google.common.testing.NullPointerTester;
import com.google.common.testing.NullPointerTester.Visibility;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class StringsUtilTest
{
	@Test
	public void testThatSpacesCreatesFiveSpaces()
	{
		assertThat(spaces(5)).isEqualTo("     ");
	}

	@Test
	public void testLevenshteinDistance()
	{
		assertThat(StringsUtil.levenshteinDistance("", "")).isZero();
		assertThat(StringsUtil.levenshteinDistance("", "a")).isEqualTo(1);
		assertThat(StringsUtil.levenshteinDistance("aaapppp", "")).isEqualTo(7);
		assertThat(StringsUtil.levenshteinDistance("frog", "fog")).isEqualTo(1);
		assertThat(StringsUtil.levenshteinDistance("fly", "ant")).isEqualTo(3);
		assertThat(StringsUtil.levenshteinDistance("elephant", "hippo")).isEqualTo(7);
		assertThat(StringsUtil.levenshteinDistance("hippo", "elephant")).isEqualTo(7);
		assertThat(StringsUtil.levenshteinDistance("hippo", "zzzzzzzz")).isEqualTo(8);
		assertThat(StringsUtil.levenshteinDistance("hello", "hallo")).isEqualTo(1);
	}

	@Test
	public void testClosestMatch()
	{
		List<String> strings = asList("logging", "help", "status");
		assertThat(closestMatch("stats", strings)).isEqualTo("status");
	}

	@Test
	public void testThatShortestStringIsReturnedForEmptyInput()
	{
		List<String> strings = asList("logging", "help", "status");
		assertThat(closestMatch("", strings)).isEqualTo("help");
	}

	@Test
	public void testThatEmptyStringIsReturnedForSmallInput()
	{
		List<String> strings = asList("--logging", "--help", "");
		assertThat(closestMatch("s", strings)).isEqualTo("");
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = Explanation.FAIL_FAST)
	public void testThatNoValidOptionsIsIllegal()
	{
		List<String> strings = asList();
		closestMatch("", strings);
	}

	@Test
	public void testSortedListOfCloseMatches()
	{
		List<String> strings = asList("stageasds", "state", "status");
		assertThat(closestMatches("statos", strings, 3)).isEqualTo(Arrays.asList("status", "state"));

		assertThat(closestMatches("foo", Collections.<String>emptyList(), 5)).isEmpty();
	}

	@Test
	public void testToLowerCase()
	{
		List<String> strings = asList("ABC", "Def");
		assertThat(toLowerCase(strings)).isEqualTo(asList("abc", "def"));
	}

	@Test
	public void testTextsForPositionalNumbers()
	{
		assertThat(numberToPositionalString(0)).isEqualTo("zeroth");
		assertThat(numberToPositionalString(1)).isEqualTo("first");
		assertThat(numberToPositionalString(2)).isEqualTo("second");
		assertThat(numberToPositionalString(3)).isEqualTo("third");
		assertThat(numberToPositionalString(4)).isEqualTo("fourth");
		assertThat(numberToPositionalString(5)).isEqualTo("fifth");
		assertThat(numberToPositionalString(6)).isEqualTo("6th");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testThatNegativeNumberCantBePositional()
	{
		numberToPositionalString(-1);
	}

	@Test
	public void testUtilityClassDesign()
	{
		new NullPointerTester().testStaticMethods(StringsUtil.class, Visibility.PACKAGE);
		UtilityClassTester.testUtilityClassDesign(StringsUtil.class);
	}
}
