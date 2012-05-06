package se.j4j.argumentparser.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.j4j.argumentparser.internal.StringComparison;
import se.j4j.argumentparser.internal.StringsUtil;

public class TestStrings
{
	@Test
	public void testAppendSpaces()
	{
		StringBuilder builder = new StringBuilder();
		StringsUtil.appendSpaces(5, builder);
		assertThat(builder.toString()).isEqualTo("     ");
	}

	@Test
	public void testFuzzyMatching()
	{
		String input = "stats";
		List<String> strings = com.google.common.collect.Lists.newArrayList("logging", "help", "status");

		assertThat(StringComparison.closestMatch(input, strings)).isEqualTo("status");
	}

	@Test
	public void testToLowerCase()
	{
		List<String> strings = Arrays.asList("ABC", "Def");
		assertThat(StringsUtil.toLowerCase(strings)).isEqualTo(Arrays.asList("abc", "def"));
	}
}
