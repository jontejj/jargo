package se.j4j.argumentparser.internal;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.internal.StringsUtil.closestMatch;
import static se.j4j.argumentparser.internal.StringsUtil.spaces;
import static se.j4j.argumentparser.internal.StringsUtil.toLowerCase;

import java.util.List;

import org.junit.Test;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TestStrings
{
	@Test
	public void testThatSpacesCreatesFiveSpaces()
	{
		assertThat(spaces(5)).isEqualTo("     ");
	}

	@Test
	public void testFuzzyMatching()
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
	@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED", justification = "Expecting an exception instead of a return")
	public void testThatNoValidOptionsIsIllegal()
	{
		List<String> strings = asList();
		closestMatch("", strings);
	}

	@Test
	public void testToLowerCase()
	{
		List<String> strings = asList("ABC", "Def");
		assertThat(toLowerCase(strings)).isEqualTo(asList("abc", "def"));
	}
}
