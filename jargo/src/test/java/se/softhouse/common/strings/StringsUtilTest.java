/* Copyright 2013 Jonatan Jönsson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
*/
package se.softhouse.common.strings;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static se.softhouse.common.strings.StringsUtil.closestMatch;
import static se.softhouse.common.strings.StringsUtil.closestMatches;
import static se.softhouse.common.strings.StringsUtil.numberToPositionalString;
import static se.softhouse.common.strings.StringsUtil.pointingAtIndex;
import static se.softhouse.common.strings.StringsUtil.spaces;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import se.softhouse.common.strings.StringsUtil;
import se.softhouse.common.testlib.Explanation;
import se.softhouse.common.testlib.UtilityClassTester;

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

	@Test
	public void testThatPointingAtIndexProducesSpacesBeforeThePointer()
	{
		assertThat(pointingAtIndex(0)).isEqualTo("^");
		assertThat(pointingAtIndex(1)).isEqualTo(" ^");
		assertThat(pointingAtIndex(2)).isEqualTo("  ^");
		try
		{
			pointingAtIndex(-1);
			fail("-1 should be an invalid index to point at");
		}
		catch(IllegalArgumentException expected)
		{

		}
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
