package se.j4j.argumentparser.utils;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static se.j4j.argumentparser.internal.StringComparison.closestMatch;
import static se.j4j.argumentparser.internal.StringsUtil.spaces;
import static se.j4j.argumentparser.internal.StringsUtil.toLowerCase;

import java.util.List;

import org.junit.Test;

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
	public void testToLowerCase()
	{
		List<String> strings = asList("ABC", "Def");
		assertThat(toLowerCase(strings)).isEqualTo(asList("abc", "def"));
	}
}
